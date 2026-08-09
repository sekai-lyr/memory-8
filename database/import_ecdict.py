import argparse
import csv
import os
import re
import subprocess
import sys


WORD_PATTERN = re.compile(r"^[A-Za-z][A-Za-z'-]{0,99}$")
POS_PATTERN = re.compile(r"^(abbr|adj|adv|ad|a|aux|conj|int|n|num|prep|pron|v|vi|vt|modal|pl)\.\s*", re.I)


def parse_args():
    parser = argparse.ArgumentParser(description="Import ECDICT CSV into sekai_word_words.")
    parser.add_argument("--csv", required=True, help="Path to ecdict.csv")
    parser.add_argument("--mysql", default="mysql", help="mysql CLI path")
    parser.add_argument("--database", default="sekai_friend")
    parser.add_argument("--user", default="root")
    parser.add_argument("--password", default="123456")
    parser.add_argument("--batch-size", type=int, default=1000)
    return parser.parse_args()


def has_chinese(value):
    return any("\u4e00" <= ch <= "\u9fff" for ch in value or "")


def clean_text(value, limit):
    if not value:
        return None
    value = value.replace("\\n", "；").replace("\r", "；").replace("\n", "；")
    value = re.sub(r"\s+", " ", value).strip(" ；;")
    return value[:limit] if value else None


def clean_translation(value):
    value = clean_text(value, 2000)
    if not value:
        return None
    parts = []
    for part in re.split(r"[；;]+", value):
        part = part.strip()
        if not part or part.startswith("[网络]"):
            continue
        part = POS_PATTERN.sub("", part).strip()
        if part:
            parts.append(part)
    result = "；".join(parts)
    return result[:500] if has_chinese(result) else None


def category_for(row):
    tag = (row.get("tag") or "").lower()
    if any(item in tag for item in ("toefl", "ielts", "gre", "sat")):
        return "ECDICT 留学词汇"
    if any(item in tag for item in ("cet4", "cet6", "ky")):
        return "ECDICT 考试词汇"
    if any(item in tag for item in ("zk", "gk")):
        return "ECDICT 中高考"
    try:
        frq = int(row.get("frq") or 0)
        bnc = int(row.get("bnc") or 0)
    except ValueError:
        frq = 0
        bnc = 0
    if frq > 0 or bnc > 0:
        return "ECDICT 常用词"
    return "ECDICT 扩展词库"


def difficulty_for(row, word):
    tag = (row.get("tag") or "").lower()
    if any(item in tag for item in ("gre", "toefl", "ielts", "sat")) or len(word) >= 12:
        return "hard"
    if any(item in tag for item in ("cet4", "cet6", "ky", "gk")) or len(word) >= 8:
        return "medium"
    return "easy"


def sql_value(value):
    if value is None:
        return "NULL"
    value = value.replace("\x00", "").replace("\\", "\\\\").replace("'", "''")
    return f"'{value}'"


def row_to_values(row):
    word = (row.get("word") or "").strip()
    if not WORD_PATTERN.match(word):
        return None
    translation = clean_translation(row.get("translation"))
    if not translation:
        return None
    phonetic = clean_text(row.get("phonetic"), 100)
    definition = clean_text(row.get("definition"), 1000)
    category = category_for(row)
    difficulty = difficulty_for(row, word)
    values = [word, translation, phonetic, definition, category, difficulty]
    return "(" + ",".join(sql_value(value) for value in values) + ")"


def flush(process, batch):
    if not batch:
        return 0
    sql = (
        "INSERT IGNORE INTO sekai_word_words "
        "(english, chinese, phonetic, example_sentence, category, difficulty) VALUES\n"
        + ",\n".join(batch)
        + ";\n"
    )
    process.stdin.write(sql)
    process.stdin.flush()
    return len(batch)


def main():
    args = parse_args()
    if not os.path.exists(args.csv):
        raise SystemExit(f"CSV not found: {args.csv}")
    command = [
        args.mysql,
        f"-u{args.user}",
        f"-p{args.password}",
        "--default-character-set=utf8mb4",
        args.database,
    ]
    process = subprocess.Popen(command, stdin=subprocess.PIPE, text=True, encoding="utf-8")
    process.stdin.write("SET NAMES utf8mb4;\n")
    batch = []
    scanned = 0
    prepared = 0
    with open(args.csv, "r", encoding="utf-8-sig", newline="") as file:
        reader = csv.DictReader(file)
        for row in reader:
            scanned += 1
            values = row_to_values(row)
            if not values:
                continue
            batch.append(values)
            prepared += 1
            if len(batch) >= args.batch_size:
                flush(process, batch)
                batch.clear()
                if prepared % 50000 == 0:
                    print(f"prepared={prepared} scanned={scanned}", flush=True)
    flush(process, batch)
    process.stdin.close()
    code = process.wait()
    if code != 0:
        raise SystemExit(code)
    print(f"done scanned={scanned} prepared={prepared}", flush=True)


if __name__ == "__main__":
    try:
        main()
    except BrokenPipeError:
        sys.exit(1)
