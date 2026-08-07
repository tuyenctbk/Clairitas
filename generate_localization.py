import os
import csv

languages = [
    ("es", "Spanish", "Sift: Noticias SNR Alto", "Noticias curadas con alta relación señal-ruido.", "Sift es tu aplicación de noticias y análisis inteligente impulsada por IA. Filtra el ruido mediático, detecta exageraciones y titulares engañosos, ofrece resúmenes de audio personalizados y resúmenes estructurados para un consumo de información eficiente y de alta calidad."),
    ("fr", "French", "Sift: Actualités SNR", "Actualités filtrées et intelligence de haute précision.", "Sift est votre application d'actualités et d'analyse intelligente propulsée par l'IA. Filtrez le bruit médiatique, détectez les pièges et bénéficiez de résumés audio personnalisés pour une consommation d'information rapide et de haute qualité."),
    ("de", "German", "Sift: Nachrichten SNR", "Gefilterte Nachrichten und intelligente Analysen.", "Sift ist Ihre intelligente Nachrichten- und Analyse-App. Filtern Sie Medienlärm, erkennen Sie manipulative Schlagzeilen und nutzen Sie personalisierte Audio-Zusammenfassungen für eine effiziente Informationsaufnahme."),
    ("ja", "Japanese", "Sift: 高SNRニュース", "ノイズを排除した高度なニュース分析。", "SiftはAIを活用した高度なニュースキュレーションアプリです。メディアのノイズを除去し、偏ったヘッドラインを検出し、パーソナライズされたオーディオダイジェストを提供します。"),
    ("zh", "Chinese Simplified", "Sift: 高SNR新闻雷达", "高信噪比的智能新闻精选。", "Sift 是一款由人工智能驱动的智能新闻分析应用。它能精准过滤媒体杂音、识别诱导性标题，提供个性化音频文摘与深度解析，助您高效获取有价值的信息。"),
    ("zh-rTW", "Chinese Traditional", "Sift: 高SNR新聞雷達", "高信噪比的智能新聞精選。", "Sift 是一款由人工智慧驅動的智慧新聞分析應用。它能精準過濾媒體雜音、識別誘導性標題，提供個性化音頻文摘與深度解析，助您高效獲取有價值的資訊。"),
    ("ar", "Arabic", "Sift: أخبار عالية النقاء", "أخبار منتقاة بعناية لتقليل التشويش.", "Sift هو تطبيق الذكاء الاصطناعي لتحليل الأخبار بتركيز عالٍ على دقة المعلومات واستبعاد العناوين المضللة وتقديم ملخصات صوتية مخصصة."),
    ("hi", "Hindi", "Sift: उच्च SNR समाचार", "उच्च संकेत-से-शोर अनुपात वाले चुनिंदा समाचार।", "Sift एआई-संचालित समाचार और विश्लेषण ऐप है। यह मीडिया के शोर को फ़िल्टर करता है, भ्रामक सुर्खियों का पता लगाता है, और कुशल जानकारी उपभोग के लिए ऑडियो डाइजेस्ट प्रदान करता है।"),
    ("pt", "Portuguese", "Sift: Notícias SNR Alto", "Notícias curadas com alta relação sinal-ruído.", "Sift é o seu aplicativo de notícias e inteligência analítica alimentado por IA. Filtre o ruído da mídia, detete iscas de cliques e aproveite resumos em áudio personalizados para uma leitura eficiente."),
    ("ru", "Russian", "Sift: Новости с высоким SNR", "Качественные новости без лишнего информационного шума.", "Sift — это интеллектуальное новостное приложение на базе искусственного интеллекта. Фильтруйте медиашум, выявляйте кликбейт и слушайте персонализированные аудиовыжимки."),
    ("it", "Italian", "Sift: Notizie SNR Alto", "Notizie filtrate con alta qualità e zero rumore.", "Sift è l'app di notizie e analisi intelligente basata sull'intelligenza artificiale. Filtra il rumore mediatico, rileva titoli ingannevoli e goditi riassunti audio personalizzati."),
    ("ko", "Korean", "Sift: 고SNR 뉴스 레이더", "노이즈를 제거한 고품질 뉴스 큐레이션.", "Sift는 AI 기반의 지능형 뉴스 분석 앱입니다. 미디어 노이즈를 필터링하고 자극적인 헤드라인을 감지하여 고효율 뉴스 요약 및 오디오 다이제스트를 제공합니다."),
    ("nl", "Dutch", "Sift: Hoge SNR Nieuws", "Gefilterd nieuws met hoge signaal-ruisverhouding.", "Sift is uw intelligente nieuws- en analyse-app aangedreven door AI. Filter media-ruis, detecteer clickbait en geniet van gepersonaliseerde audio-samenvattingen."),
    ("pl", "Polish", "Sift: Wiadomości SNR", "Wiadomości wysokiej jakości bez szumu medialnego.", "Sift to inteligentna aplikacja informacyjna i analityczna oparta na sztucznej inteligencji. Filtruj szum medialny, wykrywaj clickbait i korzystaj ze spersonalizowanych podsumowań audio."),
    ("tr", "Turkish", "Sift: Yüksek SNR Haberler", "Gürültüden arındırılmış akıllı haber kürasyonu.", "Sift, yapay zeka destekli bir haber ve analiz uygulamasıdır. Medya gürültüsünü filtreleyin, yanıltıcı başlıkları tespit edin ve kişiselleştirilmiş sesli özetlerin tadını çıkarın."),
    ("vi", "Vietnamese", "Sift: Tin Tức SNR Cao", "Tin tức chọn lọc với tỷ lệ tín hiệu trên nhiễu cao.", "Sift là ứng dụng tin tức và phân tích thông minh hỗ trợ bởi AI. Lọc nhiễu truyền thông, phát hiện tin giật gân, và cung cấp bản tin âm thanh cá nhân hóa giúp bạn tiếp thu thông tin hiệu quả."),
    ("th", "Thai", "Sift: ข่าวกรอง SNR สูง", "ข่าวคัดสรรคุณภาพสูงปราศจากเสียงรบกวน", "Sift เป็นแอปพลิเคชันวิเคราะห์และคัดสรรข่าวกรองด้วย AI กรองเสียงรบกวนจากสื่อ ตรวจจับพาดหัวข่าวล่อลวง และสรุปเสียงส่วนตัวเพื่อการเสพข้อมูลที่มีประสิทธิภาพ"),
    ("id", "Indonesian", "Sift: Berita SNR Tinggi", "Kurasi berita berkualitas tinggi tanpa kebisingan.", "Sift adalah aplikasi berita dan analisis cerdas berbasis AI. Saring kebisingan media, deteksi clickbait, dan nikmati ringkasan audio yang dipersonalisasi untuk konsumsi informasi yang efisien."),
    ("uk", "Ukrainian", "Sift: Новини з високим SNR", "Якісні новини без інформаційного шуму.", "Sift — це інтелектуальний додаток для новин та аналізу на базі штучного інтелекту. Фільтруйте медіашум та отримуйте персоналізовані аудіовижимки."),
    ("sv", "Swedish", "Sift: Hög SNR Nyheter", "Kurerade nyheter med hög signal-brus-relation.", "Sift är din AI-drivna nyhets- och analysapp. Filtrera bort mediebrus, upptäck clickbait och njut av personliga ljudsammanfattningar."),
    ("cs", "Czech", "Sift: Zprávy s vysokým SNR", "Kvalitní zprávy bez mediálního šumu.", "Sift je inteligentní zpravodajská a analytická aplikace s podporou AI. Filtrujte šum, detekujte clickbait a užívejte si personalizovaná audio shrnutí."),
    ("el", "Greek", "Sift: Ειδήσεις High SNR", "Επιλεγμένες ειδήσεις υψηλς ποιότητας.", "Sift είναι η εφαρμογή ειδήσεων και ανάλυσης με τεχνητή νοημοσύνη. Φιλτράρετε τον θόρυβο των MME και απολαύστε εξατομικευμένες περιλήψεις ήχου."),
    ("hu", "Hungarian", "Sift: Magas SNR Hírek", "Kiváló minőségű hírek zajmentesen.", "Sift egy mesterséges intelligenciával támogatott hír- és elemző alkalmazás. Szűrje ki a zajt és élvezze a személyre szabott hangalapú összefoglalókat."),
    ("ro", "Romanian", "Sift: Știri SNR Ridicat", "Știri filtrate cu o calitate excelentă.", "Sift este aplicația dvs. de știri și analiză bazată pe AI. Filtrați zgomotul media și bucurați-vă de rezumate audio personalizate."),
    ("da", "Danish", "Sift: Høj SNR Nyheder", "Kuraterede nyheder med minimalt støj.", "Sift er din AI-drevne nyheds- og analyseapp. Filtrer mediestøj, detekter clickbait og lyt til personlige lydresuméer."),
    ("fi", "Finnish", "Sift: Korkean SNR Uutiset", "Laadukkaat uutiset ilman mediakohinaa.", "Sift on tekoälypohjainen uutisten- ja analyysisovellus. Suodata mediakohina ja nauti henkilökohtaisista äänikoosteista."),
    ("nb", "Norwegian", "Sift: Høy SNR Nyheter", "Kuraterte nyheter med høy presisjon.", "Sift er din AI-drevne nyhets- og analyseapp. Filtrer bort mediestøy og få personlige lydoppsummeringer."),
    ("sk", "Slovak", "Sift: Správy s vysokým SNR", "Kvalitné správy bez mediálneho šumu.", "Sift je inteligentná spravodajská a analytická aplikácia s podporou AI pre efektívny príjem informácií."),
    ("bg", "Bulgarian", "Sift: Новини с висок SNR", "Качествени новини без излишен шум.", "Sift е интелигентно приложение за новини и анализ с изкуствен интелект. Филтрирайте медийния шум и слушайте персонализирани аудиорезюмета."),
    ("hr", "Croatian", "Sift: Vijesti Visokog SNR-a", "Kvalitetne vijesti bez medijske buke.", "Sift je inteligentna aplikacija za vijesti i analizu pokretana umjetnom inteligencijom."),
    ("sr", "Serbian", "Sift: Vesti Visokog SNR-a", "Kvalitetne vesti bez medijske buke.", "Sift je inteligentna aplikacija za vesti i analizu pokretana veštačkom inteligencijom."),
    ("lt", "Lithuanian", "Sift: Aukšto SNR Naujienos", "Kokybiškos naujienos be medijos triukšmo.", "Sift yra dirbtinio intelekto valdoma naujienų ir analizės programėlė."),
    ("lv", "Latvian", "Sift: Augsta SNR Ziņas", "Kvalitatīvas ziņas bez lieka trokšņa.", "Sift ir mākslīgā intelektā balstīta ziņu un analīzes lietotne."),
    ("et", "Estonian", "Sift: Kõrge SNR Uudised", "Kvaliteetsed uudised ilma meediakärata.", "Sift on tehisintellektil põhinev uudiste- ja analüüsirakendus."),
    ("sl", "Slovenian", "Sift: Novice Visokega SNR", "Kakovostne novice brez medijskega hrupa.", "Sift je pametna aplikacija za novice in analizo na podlagi umetne inteligence."),
    ("he", "Hebrew", "Sift: חדשות SNR גבוה", "חדשות איכותיות מסוננות בקפידה.", "Sift הוא יישום חדשות וניתוח חכם מבוסס בינה מלאכותית הסינון הרעש המדיה."),
    ("fa", "Persian", "Sift: اخبار SNR بالا", "اخبار با کیفیت بالا و بدون سر و صدا.", "Sift برنامه هوشمند اخبار و تحلیل مبتنی بر هوش مصنوعی برای مصرف کارآمد اطلاعات است."),
    ("bn", "Bengali", "Sift: উচ্চ SNR সংবাদ", "শব্দহীন উচ্চ মানের কিरेटेड সংবাদ।", "Sift হল একটি এআই-চালিত সংবাদ এবং বিশ্লেষণ অ্যাপ যা মিডিয়ার শব্দ ফিল্টার করে।"),
    ("ur", "Urdu", "Sift: اعلی SNR خبریں", "شور کے بغیر اعلیٰ معیار کی خبریں۔", "Sift ایک AI سے چلنے والی خبروں اور تجزیہ کی ایپ ہے جو میڈیا کے شور کو فلتر کرتی ہے۔"),
    ("ta", "Tamil", "Sift: உயர் SNR செய்தி", "சத்தம் இல்லாத உயர் தரமான செய்திகள்.", "Sift என்பது AI இயங்கும் செய்தி மற்றும் பகுப்பாய்வு பயன்பாடாகும்."),
    ("te", "Telugu", "Sift: అధిక SNR వార్తలు", "శబ్దం లేని అధిక నాణ్యత వార్తలు.", "Sift అనేది AI ఆధారిత వార్తలు మరియు విశ్లేషణల యాప్."),
    ("ml", "Malayalam", "Sift: ഉയർന്ന SNR വാർത്തകൾ", "ശബ്ദമില്ലാത്ത ഉയർന്ന നിലവാരമുള്ള വാർത്തകൾ.", "Sift എന്നത് AI പവർ ചെയ്യുന്ന വാർത്താ വിശകലന ആപ്പാണ്."),
    ("kn", "Kannada", "Sift: მაღალი SNR ახალი ამბები", "ಶಬ್ದವಿಲ್ಲದ ಉತ್ತಮ ಗುಣಮಟ್ಟದ ಸುದ್ದಿ.", "Sift ಎಂಬುದು AI ಚಾಲಿತ ಸುದ್ದಿ ಮತ್ತು ವಿಶ್ಲೇಷಣೆ ಅಪ್ಲಿಕೇಶನ್ ಆಗಿದೆ."),
    ("mr", "Marathi", "Sift: उच्च SNR बातम्या", "गोंगाटाशिवाय उच्च दर्जाच्या बातम्या.", "Sift हे AI-संचालित बातम्या आणि विश्लेषण ॲप आहे."),
    ("gu", "Gujarati", "Sift: ઉચ્ચ SNR સમાચાર", "ઘોங்காડા વગરના ઉચ્ચ ગુણવત્તાવાળા સમાચાર.", "Sift એ AI-સંચાલિત સમાચાર અને વિશ્લેષણ એપ્લિકેશન છે."),
    ("pa", "Punjabi", "Sift: ਉੱਚ SNR ਖਬਰਾਂ", "ਸ਼ੋਰ ਤੋਂ ਬினਾਂ ਉੱਚ ਗੁਣਵੱਤਾ ਖਬਰਾਂ.", "Sift ਇੱਕ AI-संचालित ਖ਼ਬਰਾਂ ਅਤੇ ਵਿਸ਼ਲੇਸ਼ਣ ਐਪ ਹੈ।"),
    ("sw", "Swahili", "Sift: Habari za SNR ya Juu", "Habari za ubora wa juu bila kelele.", "Sift ni programu ya habari na uchភាគි inayoendeshwa na AI."),
    ("tl", "Tagalog", "Sift: Mataas na SNR Balita", "Na-curate na balita na walang ing", "Sift ay isang AI-powered na app ng balita at pagsusuri."),
    ("ms", "Malay", "Sift: Berita SNR Tinggi", "Berita berkualiti tinggi tanpa gangguan.", "Sift ialah aplikasi berita dan analisis dikuasakan AI."),
    ("af", "Afrikaans", "Sift: Hoë SNR Nuus", "Gekureerde nuus sonder geraas.", "Sift is jou KI-gedrewe nuus- en ontledingstoepassing."),
    ("sq", "Albanian", "Sift: Lajme SNR e Lartë", "Lajme të zgjedhura me cilësi të lartë.", "Sift është aplikacioni juaj i lajmeve dhe analizave i mundësuar nga AI."),
    ("am", "Amharic", "Sift: ከፍተኛ SNR ዜና", "ከፍተኛ ጥራት ያለው የተመረጠ ዜና።", "Sift በAI የተמራ ዜና እና ትንተና መተግበሪያ ነው።"),
    ("hy", "Armenian", "Sift: Բարձր SNR Նորություններ", "Բարձրորակ ընտրանի նորություններ առանց աղմուկի:", "Sift-ը արհեստական ​​բանականությամբ աշխատող լուրերի հավելված է:"),
    ("az", "Azerbaijani", "Sift: Yüksək SNR Xəbərlər", "Səssiz və yüksək keyfiyyətli xəbərlər.", "Sift süni intellektə əsaslanan xəbər və təhlil tətbiqidir."),
    ("eu", "Basque", "Sift: SNR Handiko Albisteak", "Zaratarik gabeko kalitatezko albisteak.", "Sift AI-vel bultzatutako albiste eta analisiaren aplikazioa da."),
    ("be", "Belarusian", "Sift: Навіны з высокім SNR", "Якасныя навіны без медыйнага шуму.", "Sift — гэта інтэлектуальнае прыкладанне для навін на базе штучнага інтэлекту."),
    ("bs", "Bosnian", "Sift: Vijesti Visokog SNR-a", "Kvalitetne vijesti bez medijske buke.", "Sift je inteligentna aplikacija za vijesti i analizu pokretana vještačkom inteligencijom."),
    ("ca", "Catalan", "Sift: Notícies SNR Alt", "Notícies comissades d'alta qualitat.", "Sift és la vostra aplicació de notícies i anàlisi impulsada per IA."),
    ("gl", "Galician", "Sift: Novas SNR Alto", "Novas filtradas de alta calidade.", "Sift é a túa aplicación de novas e análise impulsada por intelixencia artificial."),
    ("is", "Icelandic", "Sift: Hár SNR Fréttir", "Gæðafréttir án miðlunarhávaða.", "Sift er gervigreindardrifið frétta- og greiningarforrit.")
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

# Generate res/values-<lang>/strings.xml for each language
for code, name, app_name, short_desc, full_desc in languages:
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
