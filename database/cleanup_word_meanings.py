import subprocess


MYSQL = ["mysql", "-uroot", "-p123456", "--default-character-set=utf8mb4", "-N", "sekai_friend"]
PREFIX_PATTERN = r"(^|；)[[:space:]]*(abbr|adj|adv|ad|a|aux|conj|int|n|num|prep|pron|v|vi|vt|modal|pl)\.[[:space:]]*"


def run(sql):
    result = subprocess.run(MYSQL, input=sql, text=True, encoding="utf-8", capture_output=True)
    if result.returncode != 0:
        raise SystemExit(result.stderr)
    return result.stdout.strip()


def main():
    sample = run(
        "SELECT REGEXP_REPLACE('a. 美丽的；n. 人', "
        f"'{PREFIX_PATTERN}', '$1');\n"
    )
    if sample != "美丽的；人":
        raise SystemExit(f"Unexpected REGEXP_REPLACE result: {sample}")
    run(
        "UPDATE sekai_word_words "
        f"SET chinese = TRIM(BOTH '；' FROM REGEXP_REPLACE(chinese, '{PREFIX_PATTERN}', '$1')) "
        "WHERE category LIKE 'ECDICT %';\n"
    )
    print("cleaned")


if __name__ == "__main__":
    main()
