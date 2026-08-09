# 馃摉 English Word Learning 路 鑻辫鍗曡瘝瀛︿範绯荤粺

> **Spring Boot + JPA 鍚庣 路 Vue 3 鍓嶇 路 闅忔満鍑洪 路 閿欓鏈?路 CSV 璇嶅簱瀵煎叆**
> Full-stack vocabulary learning system with smart answer checking & wrong-answer notebook

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-3.x-6DB33F)](https://spring.io/projects/spring-data-jpa)
[![Vue 3](https://img.shields.io/badge/Vue%203-Composition-brightgreen)](https://vuejs.org/)
[![Axios](https://img.shields.io/badge/Axios-1.x-5A29E4)](https://axios-http.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8-4479A1)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

鍓嶅悗绔垎绂荤殑鑻辫鍗曡瘝瀛︿範 Web 绯荤粺锛氬悗绔?**Spring Boot + Spring Data JPA + MySQL**锛屽墠绔?**Vue 3 + Axios + 鍘熺敓 CSS**锛岀粺涓€绔彛 `8088`銆傚畬鏁存敮鎸侀殢鏈哄嚭棰樸€佺簿纭瓟妗堟牎楠屻€侀敊棰樻湰銆丆SV 璇嶅簱瀵煎叆銆?
A full-stack vocabulary learning app: random quizzing, precise answer checking, a wrong-answer notebook, and CSV word-bank import.

## 鉁?鍔熻兘浜偣 Features

- 鐢ㄦ埛娉ㄥ唽/鐧诲綍锛圔Crypt 鍔犲瘑锛岃姹傚ご `X-User-Id` 鍖哄垎涓汉鏁版嵁锛堿uth
- **闅忔満鍑洪**锛氭帴鍙ｅ彧杩斿洖鑻辨枃锛屼笉鎻愬墠鏆撮湶涓枃绛旀 Random quizzing
- **鏅鸿兘绛旀鏍￠獙**锛氬閲婁箟绮剧‘鍖归厤锛堜笉浣跨敤 `contains`锛?澶氭柟闈㈢殑"涓嶄細璇垽"鏂归潰鐨?锛塖mart answer checking
- **閿欓鏈?*锛氳嚜鍔ㄦ敹褰曘€佸垹闄ゃ€佹竻绌恒€丆SV 瀵煎嚭 Wrong-answer notebook
- 涓夌缁冧範妯″紡锛氬叕鍏辫瘝搴?/ 鍙粌閿欓 / 鑷畾涔夎瘝搴?Practice modes
- 鑷畾涔夎瘝搴撴寔涔呭寲鍒?MySQL Custom word banks
- 瀛︿範璁板綍锛氭瘡杞粌涔犵粺璁?Learning stats
- 鏅鸿兘鏌ヨ锛氫腑鑻辫嚜鍔ㄨ瘑鍒紝蹇界暐澶у皬鍐?涓枃妯＄硦鏌ヨ Smart search
- 澶у瀷鑻变腑璇嶅簱瀵煎叆锛圗CDICT `ecdict.csv`锛塋arge dictionary import

## 馃搻 椤圭洰缁撴瀯 Project Structure

```text
english-word-learning
鈹溾攢鈹€ pom.xml
鈹溾攢鈹€ database
鈹?  鈹溾攢鈹€ schema.sql           # 寤鸿〃 + 绀轰緥鍏叡鍗曡瘝
鈹?  鈹斺攢鈹€ import-template.csv
鈹溾攢鈹€ src/main/java/com/example/wordlearning
鈹?  鈹溾攢鈹€ WordLearningApplication.java
鈹?  鈹溾攢鈹€ config / control / dataobject / model
鈹?  鈹溾攢鈹€ repository           # Spring Data JPA
鈹?  鈹溾攢鈹€ service / util
鈹?  鈹斺攢鈹€ ...
鈹溾攢鈹€ src/main/resources
鈹?  鈹溾攢鈹€ application.properties
鈹?  鈹斺攢鈹€ static               # 鍓嶇鏋勫缓浜х墿
鈹斺攢鈹€ frontend                 # Vue 3 婧愮爜
    鈹溾攢鈹€ package.json
    鈹斺攢鈹€ src/views
```

鏍稿績宸ュ叿绫伙細

- `util/AnswerJudgeUtil.java`锛氱瓟妗堟竻娲椾笌澶氱瓟妗堝畬鏁村尮閰?- `util/CsvImportUtil.java`锛欳SV 璇嶅簱瀵煎叆

## 鈻讹笍 杩愯姝ラ Run

1. 鍒涘缓骞跺鍏ユ暟鎹簱锛?
```bash
mysql -uroot -p < database/schema.sql
mysql -uroot -p --default-character-set=utf8mb4 sekai_friend < database/cet6_words.sql
mysql -uroot -p --default-character-set=utf8mb4 sekai_friend < database/word_bank_booster.sql
```

瀵煎叆澶у瀷寮€婧愯嫳涓瘝搴擄紙鍙€夛級锛?
```bash
python database/import_ecdict.py --csv /path/to/ecdict.csv
python database/cleanup_word_meanings.py
```

2. 鏁版嵁搴撹繛鎺ュ湪 `application.properties`锛堝瘑鐮佺敤鐜鍙橀噺 `DB_PASSWORD`锛夛細

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/sekai_friend?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:123456}
server.port=8088
```

3. 鎵撳寘鍓嶇鍒伴潤鎬佺洰褰曪紙褰撳墠宸插唴缃瀯寤轰骇鐗╋級锛?
```bash
cd frontend
npm install
npm run build
# 鎶?frontend/dist 鍐呭澶嶅埗鍒?src/main/resources/static
```

4. 鍚姩鍚庣骞惰闂細

```bash
mvn spring-boot:run
# http://localhost:8088/login
```

## 馃 璁捐瑕佺偣 Design Notes

鍓嶇 Vue 閫氳繃 Axios 璇锋眰鍚庣锛沗control` 鎺ユ敹璇锋眰 鈫?`service` 澶勭悊涓氬姟 鈫?`repository` 璁块棶 MySQL 鈫?缁勮 `model` DTO 缁熶竴杩斿洖銆傞殢鏈哄嚭棰樿蛋 `/api/words/random`锛屽彧杩斿洖 `id` 鍜?`english`锛涚瓟妗堟牎楠屾椂鍚庣鎸変腑鏂囧垎鍙枫€佽嫳鏂囧垎鍙枫€侀】鍙枫€侀€楀彿鎷嗗垎澶氫釜閲婁箟锛屽彧鍋氬畬鏁村尮閰嶃€?
## 馃敪 鍚庣画鎵╁睍鏂瑰悜 Roadmap

鍙戦煶鍔熻兘 路 闊虫爣鎾斁 路 渚嬪彞鏈楄 路 瀛︿範鏃ュ巻 路 楂橀閿欓浼樺厛澶嶄範 路 Excel 瀵煎嚭閿欓鏈?路 AI/鍚戦噺璇箟鍒ゆ柇

## 馃搫 License

[MIT](LICENSE) 漏 2026 [sekai-lyr](https://github.com/sekai-lyr)


<p align="center">
  <img src="screenshots/demo.gif" alt="Demo" width="720"/>
</p>
---

**猸?If this project helped you, star it! 濡傛灉杩欎釜椤圭洰瀵逛綘鏈夊府鍔╋紝娆㈣繋 Star锛?*

