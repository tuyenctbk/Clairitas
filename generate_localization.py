import os
import csv

languages = [
    # 0. English (en-US) - Primary / Default
    ("en-US", "English (United States)", "Sift: High SNR News Radar", "Filtered news and AI analysis without media noise or clickbait.",
     "Sift is an AI-powered news analysis and curation application. It filters media noise, identifies clickbait and sensational headlines, and delivers structured briefings and audio digests for efficient information consumption."),
    
    # 1. Spanish (es)
    ("es", "Spanish", "Sift: Noticias SNR Alto", "Noticias filtradas y análisis con IA sin ruido mediático.",
     "Sift es una aplicación de análisis y selección de noticias impulsada por IA. Filtra el ruido mediático, detecta titulares sensacionalistas y ofrece resúmenes estructurados y notas de audio para un consumo de información eficiente."),

    # 2. French (fr)
    ("fr", "French", "Sift: Actualités SNR", "Actualités filtrées et analyses IA sans bruit médiatique.",
     "Sift est une application d'analyse et de sélection d'actualités propulsée par l'IA. Elle filtre le bruit médiatique, détecte les titres sensationnalistes et propose des résumés audio et structurés pour un suivi efficace de l'information."),

    # 3. German (de)
    ("de", "German", "Sift: Nachrichten SNR", "Gefilterte Nachrichten und KI-Analysen ohne Medienlärm.",
     "Sift ist eine KI-gestützte Nachrichten- und Analyse-App. Sie filtert Medienrauschen, erkennt reißerische Schlagzeilen und bietet strukturierte Zusammenfassungen sowie Audio-Briefings für eine effiziente Informationsaufnahme."),

    # 4. Japanese (ja)
    ("ja", "Japanese", "Sift: 高SNRニュース", "ノイズを排除したAIニュース分析と要約。",
     "SiftはAIを活用したニュース分析・キュレーションアプリです。メディアのノイズや釣り見出しを検出し、構造化された要約と音声ダイジェストを提供して、効率的な情報収集をサポートします。"),

    # 5. Chinese Simplified (zh)
    ("zh", "Chinese Simplified", "Sift: 高SNR新闻雷达", "过滤媒体杂音的智能新闻分析与精选。",
     "Sift 是一款由人工智能驱动的新闻分析与精选应用。它能够过滤媒体杂音、识别标题党与夸大报道，并提供结构化要点与语音文摘，助您高效获取核心资讯。"),

    # 6. Chinese Traditional (zh-rTW)
    ("zh-rTW", "Chinese Traditional", "Sift: 高SNR新聞雷達", "過濾媒體雜音的智慧新聞分析與精選。",
     "Sift 是一款由人工智慧驅動的新聞分析與精選應用。它能夠過濾媒體雜音、識別標題黨與誇大報導，並提供結構化要點與語音文摘，助您高效獲取核心資訊。"),

    # 7. Arabic (ar)
    ("ar", "Arabic", "Sift: أخبار عالية النقاء", "أخبار مفلترة وتحليل ذكي بدون تشويش إعلامي.",
     "Sift هو تطبيق لتحليل واختيار الأخبار مدعوم بالذكاء الاصطناعي. يقوم بتصفية الضجيج الإعلامي، وكشف العناوين المضللة والمثيرة، وتقديم ملخصات نصية وصوتية لمتابعة الأخبار بفاعلية."),

    # 8. Hindi (hi)
    ("hi", "Hindi", "Sift: उच्च SNR समाचार", "बिना शोर के एआई-आधारित फ़िल्टर किए गए समाचार।",
     "Sift एक एआई-संचालित समाचार विश्लेषण और क्यूरेशन ऐप है। यह मीडिया के शोर को फ़िल्टर करता है, भ्रामक सुर्खियों का पता लगाता है, और संरचित सारांश व ऑडियो डाइजेस्ट प्रदान करता है।"),

    # 9. Portuguese (pt)
    ("pt", "Portuguese", "Sift: Notícias SNR Alto", "Notícias filtradas e análise por IA sem ruído da mídia.",
     "Sift é um aplicativo de análise e curadoria de notícias alimentado por IA. Ele filtra o ruído da mídia, identifica manchetes sensacionalistas e oferece resumos estruturados e em áudio para um consumo de informações eficiente."),

    # 10. Russian (ru)
    ("ru", "Russian", "Sift: Новости с высоким SNR", "Новости и ИИ-анализ без информационного шума.",
     "Sift — это приложение для анализа и отбора новостей на базе искусственного интеллекта. Оно фильтрует медиашум, выявляет кликбейт и предлагает структурированные сводки и аудиовыжимки для эффективного чтения."),

    # 11. Italian (it)
    ("it", "Italian", "Sift: Notizie SNR Alto", "Notizie filtrate e analisi IA senza rumore mediatico.",
     "Sift è un'applicazione di analisi e selezione delle notizie basata sull'IA. Filtra il rumore mediatico, rileva titoli sensazionalistici e offre riassunti strutturati e audio per una consultazione efficiente."),

    # 12. Korean (ko)
    ("ko", "Korean", "Sift: 고SNR 뉴스 레이더", "노이즈를 필터링한 AI 뉴스 분석 및 큐레이션.",
     "Sift는 AI 기반 뉴스 분석 및 큐레이션 앱입니다. 미디어 노이즈와 낚시성 헤드라인을 감지하여 구조화된 요약과 오디오 브리핑을 제공함으로써 효율적인 정보 습득을 돕습니다."),

    # 13. Dutch (nl)
    ("nl", "Dutch", "Sift: Hoge SNR Nieuws", "Gefilterd nieuws en AI-analyse zonder mediakabaal.",
     "Sift is een door AI aangedreven app voor nieuwscuratie en -analyse. De app filtert mediaruis, herkent sensationele koppen en biedt gestructureerde samenvattingen en audio-overzichten voor een efficiënte nieuwservaring."),

    # 14. Polish (pl)
    ("pl", "Polish", "Sift: Wiadomości SNR", "Filtrowane wiadomości i analizy AI bez szumu mediów.",
     "Sift to aplikacja do analizy i selekcji wiadomości oparta na sztucznej inteligencji. Filtruje szum medialny, wykrywa clickbait i oferuje uporządkowane podsumowania oraz briefingi audio."),

    # 15. Turkish (tr)
    ("tr", "Turkish", "Sift: Yüksek SNR Haberler", "Medya gürültüsünden arındırılmış yapay zeka haberleri.",
     "Sift, yapay zeka destekli bir haber analiz ve kürasyon uygulamasıdır. Medya gürültüsünü filtreler, yanıltıcı başlıkları tespit eder ve yapılandırılmış özetler ile sesli bültenler sunar."),

    # 16. Vietnamese (vi)
    ("vi", "Vietnamese", "Sift: Tin Tức SNR Cao", "Tin tức chọn lọc và phân tích AI không nhiễu thông tin.",
     "Sift là ứng dụng phân tích và chọn lọc tin tức hỗ trợ bởi AI. Ứng dụng lọc nhiễu truyền thông, phát hiện tiêu đề giật gân, đồng thời cung cấp tóm tắt bài viết và bản tin âm thanh giúp bạn nắm bắt thông tin hiệu quả."),

    # 17. Thai (th)
    ("th", "Thai", "Sift: ข่าวกรอง SNR สูง", "ข่าวกรองและการวิเคราะห์ด้วย AI ปราศจากเสียงรบกวน",
     "Sift เป็นแอปพลิเคชันวิเคราะห์และคัดสรรข่าวด้วย AI ช่วยกรองเสียงรบกวนจากสื่อ ตรวจจับพาดหัวข่าวล่อลวง และนำเสนอสรุปเนื้อหาพร้อมเสียงบรรยายเพื่อการรับข้อมูลที่มีประสิทธิภาพ"),

    # 18. Indonesian (id)
    ("id", "Indonesian", "Sift: Berita SNR Tinggi", "Berita terfilter dan analisis AI tanpa kebisingan media.",
     "Sift adalah aplikasi kurasi dan analisis berita berbasis AI. Aplikasi ini menyaring kebisingan media, mendeteksi judul umpan klik, serta menyediakan ringkasan terstruktur dan audio untuk konsumsi informasi yang efisien."),

    # 19. Ukrainian (uk)
    ("uk", "Ukrainian", "Sift: Новини з високим SNR", "Відфільтровані новини та ШІ-аналіз без медіашуму.",
     "Sift — це додаток для аналізу та відбору новин на базі штучного інтелекту. Він фільтрує медійний шум, виявляє клікбейт і пропонує структуровані підсумки та аудіовижимки."),

    # 20. Swedish (sv)
    ("sv", "Swedish", "Sift: Hög SNR Nyheter", "Filtrerade nyheter och AI-analys utan mediebrus.",
     "Sift är en AI-driven app för nyhetsanalys och kurering. Den filtrerar bort mediebrus, identifierar klickbeten och ger strukturerade sammanfattningar samt ljudsammanfattningar för effektiv informationsläsning."),

    # 21. Czech (cs)
    ("cs", "Czech", "Sift: Zprávy s vysokým SNR", "Filtrované zprávy a AI analýza bez mediálního šumu.",
     "Sift je aplikace pro analýzu a výběr zpráv poháněná umělou inteligencí. Filtruje mediální šum, detekuje clickbait a nabízí strukturovaná shrnutí a zvukové přehledy."),

    # 22. Greek (el)
    ("el", "Greek", "Sift: Ειδήσεις High SNR", "Φιλτραρισμένες ειδήσεις και ανάλυση AI χωρίς θόρυβο.",
     "Το Sift είναι μια εφαρμογή ανάλυσης και επιλογής ειδήσεων με τεχνητή νοημοσύνη. Φιλτράρει τον θόρυβο των μέσων ενημέρωσης, εντοπίζει παραπλανητικούς τίτλους και προσφέρει δομημένες περιλήψεις και ηχητικά δελτία."),

    # 23. Hungarian (hu)
    ("hu", "Hungarian", "Sift: Magas SNR Hírek", "Szűrt hírek és MI-elemzés médiazaj nélkül.",
     "A Sift egy mesterséges intelligenciával működő hírelemző és kurációs alkalmazás. Kiszűri a médiahangzavart, azonosítja a kattintásvadász címeket, valamint strukturált és hangos összefoglalókat kínál."),

    # 24. Romanian (ro)
    ("ro", "Romanian", "Sift: Știri SNR Ridicat", "Știri filtrate și analiză AI fără zgomot media.",
     "Sift este o aplicație de analiză și selecție a știrilor bazată pe inteligență artificială. Filtrează zgomotul media, detectează titlurile de tip clickbait și oferă rezumate structurate și audio."),

    # 25. Danish (da)
    ("da", "Danish", "Sift: Høj SNR Nyheder", "Filtrerede nyheder og AI-analyse uden mediestøj.",
     "Sift er en AI-drevet app til nyhedsanalyse og kuratering. Den filtrerer mediestøj fra, registrerer clickbait og leverer strukturerede oversigter samt lydresuméer."),

    # 26. Finnish (fi)
    ("fi", "Finnish", "Sift: Korkean SNR Uutiset", "Suodatetut uutiset ja tekoälyanalyysi ilman kohinaa.",
     "Sift on tekoälypohjainen uutisanalyysi- ja kuratointisovellus. Se suodattaa mediakohinan, tunnistaa klikkiotsikot ja tarjoaa jäsenneltyjä tiivistelmiä sekä äänikoosteita."),

    # 27. Norwegian (nb)
    ("nb", "Norwegian", "Sift: Høy SNR Nyheter", "Filtrerte nyheter og AI-analyse uten mediestøy.",
     "Sift er en AI-drevet nyhets- og analyseapp. Den filtrerer bort mediestøy, oppdager klikkagn og leverer strukturerte sammendrag samt lydoppsummeringer for effektiv informasjonsinnhenting."),

    # 28. Slovak (sk)
    ("sk", "Slovak", "Sift: Správy s vysokým SNR", "Filtrované správy a AI analýza bez mediálneho šumu.",
     "Sift je spravodajská a analytická aplikácia s podporou umelej inteligencie. Filtruje mediálny šum, deteguje clickbait a ponúka štruktúrované zhrnutia a zvukové prehľady."),

    # 29. Bulgarian (bg)
    ("bg", "Bulgarian", "Sift: Новини с висок SNR", "Филтрирани новини и AI анализ без медиен шум.",
     "Sift е приложение за анализ и подбор на новини с изкуствен интелект. То филтрира медийния шум, засича кликбейт заглавия и предлага структурирани обобщения и аудио резюмета."),

    # 30. Croatian (hr)
    ("hr", "Croatian", "Sift: Vijesti Visokog SNR-a", "Filtrirane vijesti i AI analiza bez medijske buke.",
     "Sift je aplikacija za analizu i odabir vijesti pokretana umjetnom inteligencijom. Filtrira medijski šum, prepoznaje senzacionalističke naslove te nudi strukturirane sažetke i audio preglede."),

    # 31. Serbian (sr)
    ("sr", "Serbian", "Sift: Vesti Visokog SNR-a", "Filtrirane vesti i AI analiza bez medijske buke.",
     "Sift je aplikacija za analizu i selekciju vesti pokretana veštačkom inteligencijom. Filtrira medijski šum, prepoznaje senzacionalističke naslove i pruža strukturirane sažetke i audio preglede."),

    # 32. Lithuanian (lt)
    ("lt", "Lithuanian", "Sift: Aukšto SNR Naujienos", "Filtruotos naujienos ir DI analizė be medijos triukšmo.",
     "Sift yra dirbtinio intelekto valdoma naujienų analizės ir atrankos programėlė. Ji filtruoja medijos triukšmą, atpažįsta viliojančias antraštes ir pateikia struktūrizuotas bei garso santraukas."),

    # 33. Latvian (lv)
    ("lv", "Latvian", "Sift: Augsta SNR Ziņas", "Filtrētas ziņas un MI analīze bez lieka trokšņa.",
     "Sift ir mākslīgā intelekta darbināta ziņu analīzes un atlases lietotne. Tā filtrē mediju troksni, atpazīst klikšķēsmas virsrakstus un nodrošina strukturētus kopsavilkumus un audio pārskatus."),

    # 34. Estonian (et)
    ("et", "Estonian", "Sift: Kõrge SNR Uudised", "Filtreeritud uudised ja tehisintellekti analüüs.",
     "Sift on tehisintellektil põhinev uudiste analüüsi ja kureerimise rakendus. See filtreerib meediamüra, tuvastab klõpsusööda ning pakub struktureeritud kokkuvõtteid ja helikokkuvõtteid."),

    # 35. Slovenian (sl)
    ("sl", "Slovenian", "Sift: Novice Visokega SNR", "Filtrirane novice in analiza z umetno inteligenco.",
     "Sift je aplikacija za analizo in izbiro novic na podlagi umetne inteligence. Filtrira medijski hrup, zaznava zavajajoče naslove ter ponuja strukturirane povzetke in zvočne preglede."),

    # 36. Hebrew (he)
    ("he", "Hebrew", "Sift: חדשות SNR גבוה", "חדשות מסוננות וניתוח בינה מלאכותית ללא רעש.",
     "Sift הוא יישום לניתוח וסינון חדשות המופעל על ידי בינה מלאכותית. היישום מסנן רעשי מדיה, מזהה כותרות מפתות ומספק תקצירים מובנים ומהדורות שמע מותאמות."),

    # 37. Persian (fa)
    ("fa", "Persian", "Sift: اخبار SNR بالا", "اخبار فیلتر شده و تحلیل هوش مصنوعی بدون هیاهو.",
     "Sift یک برنامه تحلیل و گزینش اخبار مبتنی بر هوش مصنوعی است. این برنامه هیاهوی رسانه‌ای را فیلتر می‌کند، تیترهای زرد را شناسایی کرده و خلاصه‌های ساختاریافته و صوتی ارائه می‌دهد."),

    # 38. Bengali (bn)
    ("bn", "Bengali", "Sift: উচ্চ SNR সংবাদ", "মিডিয়া শব্দহীন এআই চালিত সংবাদের বিশ্লেষণ।",
     "Sift হল একটি এআই-চালিত সংবাদ বিশ্লেষণ এবং বাছাইকরণ অ্যাপ। এটি মিডিয়ার অপ্রয়োজনীয় শব্দ ফিল্টার করে, চটকদার শিরোনাম শনাক্ত করে এবং কাঠামোগত সারাংশ ও অডিও ডাইজেস্ট প্রদান করে।"),

    # 39. Urdu (ur)
    ("ur", "Urdu", "Sift: اعلی SNR خبریں", "میڈیا شور کے بغیر فلٹر شدہ خبریں اور AI تجزیہ۔",
     "Sift ایک AI سے چلنے والی خبروں کے تجزیے اور انتخاب کی ایپ ہے۔ یہ میڈیا کے شور کو فلٹر کرتی ہے، کلک بیٹ کی نشاندہی کرتی ہے اور منظم خلاصے اور آڈیو ڈائجسٹ فراہم کرتی ہے۔"),

    # 40. Tamil (ta)
    ("ta", "Tamil", "Sift: உயர் SNR செய்தி", "ஊடக இரைச்சல் இல்லாத AI செய்தி பகுப்பாய்வு.",
     "Sift என்பது AI-ஆல் இயங்கும் செய்தி பகுப்பாய்வு மற்றும் தொகுப்பு பயன்பாடாகும். இது ஊடக இரைச்சலை வடிகட்டுகிறது, கவர்ச்சிகரமான தலைப்புகளைக் கண்டறிகிறது, மேலும் ஆடியோ சுருக்கங்களை வழங்குகிறது."),

    # 41. Telugu (te)
    ("te", "Telugu", "Sift: అధిక SNR వార్తలు", "శబ్దం లేని వార్తలు మరియు AI విశ్లేషణ.",
     "Sift అనేది AI ఆధారిత వార్తల విశ్లేషణ మరియు ఎంపిక యాప్. ఇది మీడియా శబ్దాన్ని ఫిల్టర్ చేస్తుంది, క్లిక్‌బైట్ శీర్షికలను గుర్తిస్తుంది మరియు స్పష్టమైన సారాంశాలు, ఆడియో డైజెస్ట్‌లను అందిస్తుంది."),

    # 42. Malayalam (ml)
    ("ml", "Malayalam", "Sift: ഉയർന്ന SNR വാർത്തകൾ", "മാധ്യമ ശബ്ദമില്ലാത്ത AI വാർത്താ വിശകലനം.",
     "Sift എന്നത് AI അടിസ്ഥാനമാക്കിയുള്ള വാർത്താ വിശകലന ആപ്പാണ്. ഇത് അനാവശ്യ മാധ്യമ ശബ്ദങ്ങളെ ഫിൽട്ടർ ചെയ്യുകയും വഴിതെറ്റിക്കുന്ന തലക്കെട്ടുകൾ കണ്ടെത്തുകയും ഘടനാപരമായ സംഗ്രഹങ്ങളും ഓഡിയോയും നൽകുകയും ചെയ്യുന്നു."),

    # 43. Kannada (kn)
    ("kn", "Kannada", "Sift: ಹೆಚ್ಚಿನ SNR ಸುದ್ದಿ", "ಮಾಧ್ಯಮ ಶಬ್ದವಿಲ್ಲದ AI ಸುದ್ದಿ ವಿಶ್ಲೇಷಣೆ.",
     "Sift ಎಂಬುದು AI ಆಧಾರಿತ ಸುದ್ದಿ ವಿಶ್ಲೇಷಣೆ ಮತ್ತು ಆಯ್ಕೆ ಅಪ್ಲಿಕೇಶನ್ ಆಗಿದೆ. ಇದು ಮಾಧ್ಯಮ ಶಬ್ದವನ್ನು ಫಿಲ್ಟರ್ ಮಾಡುತ್ತದೆ, ಕ್ಲಿಕ್‌ಬೇಟ್ ಶೀರ್ಷಿಕೆಗಳನ್ನು ಪತ್ತೆ ಮಾಡುತ್ತದೆ ಮತ್ತು ರಚನಾತ್ಮಕ ಸಾರಾಂಶಗಳನ್ನು ನೀಡುತ್ತದೆ."),

    # 44. Marathi (mr)
    ("mr", "Marathi", "Sift: उच्च SNR बातम्या", "गोंगाटाशिवाय AI आधारित बातम्यांचे विश्लेषण.",
     "Sift हे AI-आधारित बातमी विश्लेषण आणि क्युरेशन ॲप आहे. हे मीडियाचा गोंगाट फिल्टर करते, दिशाभूल करणाऱ्या हेडलाईन्स शोधते आणि संरचित सारांश व ऑडिओ डायजेस्ट प्रदान करते."),

    # 45. Gujarati (gu)
    ("gu", "Gujarati", "Sift: ઉચ્ચ SNR સમાચાર", "ઘોંઘાટ વગરના AI-આધારિત સમાચાર અને વિશ્લેષણ.",
     "Sift એ AI-આધારિત સમાચાર વિશ્લેષણ અને ક્યુરેશન એપ્લિકેશન છે. તે મીડિયાના ઘોંઘાટને ફિલ્ટર કરે છે, ક્લિકબેટ હેડલાઇન્સ શોધે છે અને સંક્ષિપ્ત સારાંશ અને ઑડિયો ડાયજેસ્ટ પ્રદાન કરે છે."),

    # 46. Punjabi (pa)
    ("pa", "Punjabi", "Sift: ਉੱਚ SNR ਖ਼ਬਰਾਂ", "ਮੀਡੀਆ ਸ਼ੋਰ ਤੋਂ ਬਿਨਾਂ AI ਖ਼ਬਰਾਂ ਦਾ ਵਿਸ਼ਲੇਸ਼ਣ।",
     "Sift ਇੱਕ AI-ਅਧਾਰਿਤ ਖ਼ਬਰਾਂ ਦੇ ਵਿਸ਼ਲੇਸ਼ਣ ਅਤੇ ਚੋਣ ਦੀ ਐਪ ਹੈ। ਇਹ ਮੀਡੀਆ ਦੇ ਸ਼ੋਰ ਨੂੰ ਫਿਲਟਰ ਕਰਦੀ ਹੈ, ਗੁੰਮਰਾਹਕੁੰਨ ਸੁਰਖੀਆਂ ਦੀ ਪਛਾਣ ਕਰਦੀ ਹੈ ਅਤੇ ਸੰਖੇਪ ਸਾਰਾਂਸ਼ ਤੇ ਆਡੀਓ ਡਾਇਜੈਸਟ ਪ੍ਰਦਾਨ ਕਰਦੀ ਹੈ।"),

    # 47. Swahili (sw)
    ("sw", "Swahili", "Sift: Habari za SNR ya Juu", "Habari zilizochujwa na uchambuzi wa AI bila kelele.",
     "Sift ni programu ya uchambuzi na uteuzi wa habari inayoendeshwa na AI. Inachuja kelele za vyombo vya habari, inatambua vichwa vya habari vya kupotosha na inatoa muhtasari wa maandishi na sauti."),

    # 48. Tagalog (tl)
    ("tl", "Tagalog", "Sift: Mataas na SNR Balita", "Na-filter na balita at pagsusuri nang walang ingay.",
     "Ang Sift ay isang AI-powered na app para sa pagsusuri at pagpili ng balita. Sinasala nito ang ingay ng media, tinutukoy ang mga clickbait na pamagat, at nagbibigay ng mga buod at audio digest."),

    # 49. Malay (ms)
    ("ms", "Malay", "Sift: Berita SNR Tinggi", "Berita ditapis dan analisis AI tanpa gangguan media.",
     "Sift ialah aplikasi analisis dan pemilihan berita yang dikuasakan oleh AI. Ia menapis gangguan media, mengesan tajuk umpan klik, serta menyediakan ringkasan berstruktur dan audio."),

    # 50. Afrikaans (af)
    ("af", "Afrikaans", "Sift: Hoë SNR Nuus", "Gefiltreerde nuus en KI-ontleding sonder mediageraas.",
     "Sift is 'n KI-aangedrewe nuusanalise- en keuringstoepassing. Dit filtreer mediageraas, identifiseer kliekaas en bied gestruktureerde opsommings en oudio-oorsigte vir doeltreffende inligtingverbruik."),

    # 51. Albanian (sq)
    ("sq", "Albanian", "Sift: Lajme SNR e Lartë", "Lajme të filtruara dhe analizë me AI pa zhurmë mediatike.",
     "Sift është një aplikacion për analizën dhe përzgjedhjen e lajmeve i mundësuar nga AI. Ai filtron zhurmën mediatike, identifikon titujt tërheqës mashtrues dhe ofron përmbledhje të strukturuara dhe audio."),

    # 52. Amharic (am)
    ("am", "Amharic", "Sift: ከፍተኛ SNR ዜና", "ያለ ሚዲያ ጫጫታ የተጣራ ዜና እና የAI ትንተና።",
     "Sift በAI የሚሰራ የዜና ትንተና እና ማጣሪያ መተግበሪያ ነው። የሚዲያ ጫጫታን ያጣራል፣ አሳሳች አርዕስቶችን ይለያል እንዲሁም የተዋቀሩ ማጠቃለያዎችን እና የድምጽ ዳይጀስቶችን ያቀርባል።"),

    # 53. Armenian (hy)
    ("hy", "Armenian", "Sift: Բարձր SNR Նորություններ", "Զտված նորություններ և AI վերլուծություն առանց աղմուկի:",
     "Sift-ը արհեստական բանականությամբ աշխատող լուրերի վերլուծության և ընտրության հավելված է: Այն զտում է մեդիա աղմուկը, հայտնաբերում մոլորեցնող վերնագրերը և տրամադրում կառուցվածքային ամփոփագրեր ու աուդիո նյութեր:"),

    # 54. Azerbaijani (az)
    ("az", "Azerbaijani", "Sift: Yüksək SNR Xəbərlər", "Media küyü olmadan süzülmüş xəbərlər və AI təhlili.",
     "Sift süni intellekt əsaslı xəbər təhlili və seçimi tətbiqidir. Media səs-küyünü süzür, aldadıcı başlıqları müəyyən edir və strukturlaşdırılmış xülasələr ilə audio icmallar təqdim edir."),

    # 55. Basque (eu)
    ("eu", "Basque", "Sift: SNR Handiko Albisteak", "Zaratarik gabeko albiste iragaziak eta AI analisia.",
     "Sift AI bidezko albisteen azterketa eta hautaketa aplikazioa da. Komunikabideen zarata iragazten du, klik-amu tituluak hautematen ditu eta laburpen egituratuak zein audioak eskaintzen ditu."),

    # 56. Belarusian (be)
    ("be", "Belarusian", "Sift: Навіны з высокім SNR", "Адфільтраваныя навіны і ІІ-аналіз без медыяшуму.",
     "Sift — гэта праграма для аналізу і адбору навін на базе штучнага інтэлекту. Яна фільтруе медыяшум, выяўляе клікбэйт і прапануе структураваныя зводкі і аўдыявыціскі."),

    # 57. Bosnian (bs)
    ("bs", "Bosnian", "Sift: Vijesti Visokog SNR-a", "Filtrirane vijesti i AI analiza bez medijske buke.",
     "Sift je aplikacija za analizu i odabir vijesti pokretana vještačkom inteligencijom. Filtrira medijski šum, prepoznaje senzacionalističke naslove i pruža strukturirane sažetke i audio preglede."),

    # 58. Catalan (ca)
    ("ca", "Catalan", "Sift: Notícies SNR Alt", "Notícies filtrades i anàlisi amb IA sense soroll mediàtic.",
     "Sift és una aplicació d'anàlisi i selecció de notícies impulsada per IA. Filtra el soroll mediàtic, detecta titulars sensacionalistes i ofereix resums estructurats i d'àudio per a un consum eficient d'informació."),

    # 59. Galician (gl)
    ("gl", "Galician", "Sift: Novas SNR Alto", "Novas filtradas e análise con IA sen ruído mediático.",
     "Sift é unha aplicación de análise e selección de novas impulsada por IA. Filtra o ruído mediático, detecta titulares enganosos e ofrece resumos estruturados e en son para unha lectura eficiente."),

    # 60. Icelandic (is)
    ("is", "Icelandic", "Sift: Hár SNR Fréttir", "Síaðar fréttir og gervigreindargreining án miðlunarhávaða.",
     "Sift er gervigreindardrifið fréttagreiningar- og vinnsluforrit. Það síar burt fjölmiðlahávaða, greinir smellibeitur og veitir skipulagðar samantektir og hljóðupptökur fyrir skilvirka upplýsingaöflun.")
]

os.makedirs("app/src/main/res/values", exist_ok=True)

# Generate CSV for store listing
csv_path = "store_listing_60_languages.csv"
with open(csv_path, "w", newline="", encoding="utf-8") as csv_file:
    writer = csv.writer(csv_file)
    writer.writerow(["Language Code", "Language Name", "App Name (<=30)", "Short Description (<=80)", "Full Description (<4000)"])
    for code, name, app_name, short_desc, full_desc in languages:
        writer.writerow([code, name, app_name, short_desc, full_desc])

print(f"Generated {csv_path} with {len(languages)} languages.")

# Generate res/values-<lang>/strings.xml for localized app_name
for code, name, app_name, short_desc, full_desc in languages:
    if code == "en-US":
        continue
    dir_name = f"app/src/main/res/values-{code}"
    os.makedirs(dir_name, exist_ok=True)
    xml_content = f'''<resources>
    <string name="app_name">{app_name}</string>
    <string name="nav_home">Home</string>
    <string name="nav_radar">News Radar</string>
    <string name="nav_audio">Audio Digest</string>
    <string name="nav_bookmarks">Bookmarks</string>
    <string name="nav_settings">Settings</string>
    <string name="search_hint">Search articles, topics...</string>
    <string name="refresh_status">Live Feed Updated</string>
    <string name="high_snr_filter">High SNR Only</string>
    <string name="category_all">All</string>
    <string name="category_tech">Tech</string>
    <string name="category_markets">Markets</string>
    <string name="category_real_estate">Real Estate</string>
    <string name="category_business">Business</string>
    <string name="category_science">Science</string>
    <string name="time_budget_2min">2 min</string>
    <string name="time_budget_5min">5 min</string>
    <string name="time_budget_10min">10 min</string>
    <string name="mode_flash">Flash</string>
    <string name="mode_deep">Deep Dive</string>
    <string name="settings_title">Settings</string>
    <string name="settings_api_key">Gemini API Key</string>
    <string name="settings_save">Save Key</string>
    <string name="settings_saved">API Key Saved</string>
    <string name="bookmarks_title">Saved Bookmarks</string>
    <string name="radar_title">Noise &amp; Trap Radar</string>
    <string name="audio_title">Audio Briefing Digest</string>
    <string name="article_detail_title">Article Analysis</string>
    <string name="onboarding_title">Welcome to Sift</string>
    <string name="onboarding_subtitle">Signal-to-noise news curation &amp; intelligence</string>
    <string name="get_started">Get Started</string>
</resources>
'''
    with open(os.path.join(dir_name, "strings.xml"), "w", encoding="utf-8") as xml_file:
        xml_file.write(xml_content)

print(f"Generated strings.xml for {len(languages)} languages successfully.")
