package com.example.util

import com.example.ui.components.NavTab

object AppStrings {

    fun getNavTitle(tab: NavTab, langCode: String): String {
        val lang = langCode.lowercase().take(2)
        return when (tab) {
            NavTab.HOME -> when (lang) {
                "vi" -> "Bảng tin"
                "es" -> "Inicio"
                "fr" -> "Flux"
                "de" -> "Feed"
                "ja" -> "フィード"
                "ko" -> "피드"
                "zh" -> "动态"
                "pt" -> "Início"
                "it" -> "Feed"
                "hi" -> "फ़ीड"
                "ru" -> "Лента"
                "ar" -> "الرئيسية"
                "nl" -> "Feed"
                "id" -> "Berita"
                "th" -> "ฟีด"
                else -> "Feed"
            }
            NavTab.RADAR -> when (lang) {
                "vi" -> "Rada tin"
                "es" -> "Radar"
                "fr" -> "Radar"
                "de" -> "Radar"
                "ja" -> "レーダー"
                "ko" -> "레이더"
                "zh" -> "雷达"
                "pt" -> "Radar"
                "it" -> "Radar"
                "hi" -> "रडार"
                "ru" -> "Радар"
                "ar" -> "الرادار"
                "nl" -> "Radar"
                "id" -> "Radar"
                "th" -> "เรดาร์"
                else -> "Radar"
            }
            NavTab.AUDIO -> when (lang) {
                "vi" -> "Âm thanh"
                "es" -> "Audio"
                "fr" -> "Audio"
                "de" -> "Audio"
                "ja" -> "音声"
                "ko" -> "오디오"
                "zh" -> "音频"
                "pt" -> "Áudio"
                "it" -> "Audio"
                "hi" -> "ऑडियो"
                "ru" -> "Аудио"
                "ar" -> "الصوت"
                "nl" -> "Audio"
                "id" -> "Audio"
                "th" -> "เสียง"
                else -> "Audio"
            }
            NavTab.BOOKMARKS -> when (lang) {
                "vi" -> "Đã lưu"
                "es" -> "Guardados"
                "fr" -> "Favoris"
                "de" -> "Gemerkt"
                "ja" -> "保存済み"
                "ko" -> "저장됨"
                "zh" -> "收藏"
                "pt" -> "Salvos"
                "it" -> "Salvati"
                "hi" -> "सहेजे गए"
                "ru" -> "Закладки"
                "ar" -> "المحفوظات"
                "nl" -> "Bewaard"
                "id" -> "Tersimpan"
                "th" -> "บันทึกแล้ว"
                else -> "Saved"
            }
            NavTab.SETTINGS -> when (lang) {
                "vi" -> "Cài đặt"
                "es" -> "Ajustes"
                "fr" -> "Paramètres"
                "de" -> "Einstellungen"
                "ja" -> "設定"
                "ko" -> "설정"
                "zh" -> "设置"
                "pt" -> "Ajustes"
                "it" -> "Impostazioni"
                "hi" -> "सेटिंग्स"
                "ru" -> "Настройки"
                "ar" -> "الإعدادات"
                "nl" -> "Instellingen"
                "id" -> "Pengaturan"
                "th" -> "การตั้งค่า"
                else -> "Settings"
            }
        }
    }

    fun getSettingsTitle(langCode: String): String {
        return when (langCode.lowercase().take(2)) {
            "vi" -> "Cài đặt & Tùy chọn"
            "es" -> "Ajustes y Preferencias"
            "fr" -> "Paramètres & Préférences"
            "de" -> "Einstellungen & Optionen"
            "ja" -> "設定と基本設定"
            "ko" -> "설정 및 환경설정"
            "zh" -> "设置与偏好"
            "pt" -> "Configurações e Preferências"
            "it" -> "Impostazioni e Preferenze"
            "hi" -> "सेटिंग्स और प्राथमिकताएं"
            "ru" -> "Настройки и параметры"
            "ar" -> "الإعدادات والتفضيلات"
            "nl" -> "Instellingen & Voorkeuren"
            "id" -> "Pengaturan & Preferensi"
            "th" -> "การตั้งค่าและความชอบ"
            else -> "Settings & Preferences"
        }
    }

    fun getCountrySettingTitle(langCode: String): String {
        return when (langCode.lowercase().take(2)) {
            "vi" -> "Quốc gia & Khu vực mục tiêu"
            "es" -> "País y Región Objetivo"
            "fr" -> "Pays & Région Cible"
            "de" -> "Ziel-Land & Region"
            "ja" -> "対象国と地域"
            "ko" -> "대상 국가 및 지역"
            "zh" -> "目标国家与地区"
            "pt" -> "País e Região Alvo"
            "it" -> "Paese e Regione Target"
            "hi" -> "लक्ष्य देश और क्षेत्र"
            "ru" -> "Целевая страна и регион"
            "ar" -> "الدولة والمنطقة المستهدفة"
            "nl" -> "Doelland & Regio"
            "id" -> "Negara & Wilayah Target"
            "th" -> "ประเทศและภูมิภาคเป้าหมาย"
            else -> "Target Country & Region"
        }
    }

    fun getCountrySettingSubtitle(langCode: String): String {
        return when (langCode.lowercase().take(2)) {
            "vi" -> "Chọn khu vực địa lý để tối ưu hóa nguồn tin tức AI."
            "es" -> "Selecciona la región geográfica para optimizar las fuentes de noticias IA."
            "fr" -> "Sélectionnez la région géographique pour optimiser le flux d'informations IA."
            "de" -> "Wählen Sie die geografische Region zur Optimierung der KI-Nachrichten."
            "ja" -> "AIニュースソースのフィルタリングに使用する地域を選択します。"
            "ko" -> "AI 뉴스 소스 필터링에 사용할 지역을 선택하세요."
            "zh" -> "选择用于 AI 新闻源过滤的目标地理区域。"
            "pt" -> "Selecione a região geográfica para otimizar as fontes de notícias da IA."
            "it" -> "Seleziona la regione geografica per ottimizzare le fonti di notizie IA."
            "hi" -> "AI समाचार स्रोतों के लिए लक्षित भौगोलिक क्षेत्र चुनें।"
            "ru" -> "Выберите регион для оптимизации источников новостей ИИ."
            "ar" -> "اختر المنطقة الجغرافية لتخصيص مصادر الأخبار بالذكاء الاصطناعي."
            "nl" -> "Selecteer de geografische regio voor de beste AI-nieuwsbronnen."
            "id" -> "Pilih wilayah geografis untuk mengoptimalkan sumber berita AI."
            "th" -> "เลือกภูมิภาคเป้าหมายเพื่อปรับปรุงแหล่งข่าว AI"
            else -> "Select target geographical region used for AI news sources and localized curation."
        }
    }

    fun getThemeTitle(langCode: String): String {
        return when (langCode.lowercase().take(2)) {
            "vi" -> "Giao diện & Chế độ tối"
            "es" -> "Tema y Apariencia"
            "fr" -> "Thème & Apparence"
            "de" -> "Design & Erscheinungsbild"
            "ja" -> "テーマモード"
            "ko" -> "테마 모드"
            "zh" -> "主题模式"
            "pt" -> "Modo de Tema"
            "it" -> "Modalità Tema"
            "hi" -> "थीम मोड"
            "ru" -> "Тема оформления"
            "ar" -> "وضع المظهر"
            "nl" -> "Themamodus"
            "id" -> "Mode Tema"
            "th" -> "โหมดธีม"
            else -> "Theme Mode"
        }
    }

    fun getLanguageSettingTitle(langCode: String): String {
        return when (langCode.lowercase().take(2)) {
            "vi" -> "Ngôn ngữ ứng dụng & Dịch thuật"
            "es" -> "Idioma de la App y Traducción"
            "fr" -> "Langue de l'application & Traduction"
            "de" -> "App-Sprache & Übersetzung"
            "ja" -> "アプリの言語とインターフェース"
            "ko" -> "앱 언어 및 인터페이스"
            "zh" -> "应用语言与界面"
            "pt" -> "Idioma do App e Interface"
            "it" -> "Lingua dell'App e Interfaccia"
            "hi" -> "ऐप भाषा और इंटरफ़ेस"
            "ru" -> "Язык приложения и интерфейса"
            "ar" -> "لغة التطبيق والواجهة"
            "nl" -> "App-taal & Interface"
            "id" -> "Bahasa Aplikasi & Antarmuka"
            "th" -> "ภาษาแอปและอินเทอร์เฟซ"
            else -> "App Language & Interface"
        }
    }

    fun getLanguageSettingSubtitle(langCode: String): String {
        return when (langCode.lowercase().take(2)) {
            "vi" -> "Chọn ngôn ngữ mặc định cho giao diện và dịch bài viết bằng AI."
            "es" -> "Selecciona tu idioma predeterminado para la interfaz y traducción IA."
            "fr" -> "Choisissez votre langue par défaut pour l'interface et la traduction IA."
            "de" -> "Wählen Sie Ihre Standardsprache für Oberfläche und KI-Übersetzung."
            "ja" -> "ユーザーインターフェースとAI翻訳サービスのデフォルト言語を選択します。"
            "ko" -> "사용자 인터페이스 및 AI 번역 서비스의 기본 언어를 선택하세요."
            "zh" -> "选择界面和 AI 文章翻译服务的默认语言。"
            "pt" -> "Selecione o idioma padrão para a interface e tradução de IA."
            "it" -> "Seleziona la lingua predefinita per l'interfaccia e la traduzione IA."
            "hi" -> "यूज़र इंटरफ़ेस और AI अनुवाद के लिए अपनी डिफ़ॉल्ट भाषा चुनें।"
            "ru" -> "Выберите язык по умолчанию для интерфейса и ИИ-перевода."
            "ar" -> "اختر لغتك الافتراضية لواجهة المستخدم وخدمات ترجمة الذكاء الاصطناعي."
            "nl" -> "Selecteer uw standaardtaal voor de interface en AI-vertaling."
            "id" -> "Pilih bahasa default untuk antarmuka dan layanan translasi AI."
            "th" -> "เลือกภาษาเริ่มต้นสำหรับอินเทอร์เฟซผู้ใช้และการแปล AI"
            else -> "Select your default language for both the user interface and AI article translation services."
        }
    }

    fun getTypographyTitle(langCode: String): String {
        return when (langCode.lowercase().take(2)) {
            "vi" -> "Phông chữ & Kiểu hiển thị"
            "es" -> "Tipografía y Pantalla del Lector"
            "fr" -> "Typographie & Affichage de lecture"
            "de" -> "Leser-Typografie & Anzeige"
            "ja" -> "フォントと表示設定"
            "ko" -> "독서 타이포그래피 및 디스플레이"
            "zh" -> "阅读排版与显示"
            "pt" -> "Tipografia e Exibição do Leitor"
            "it" -> "Tipografia e Visualizzazione"
            "hi" -> "रीडर टाइपोग्राफी और डिस्प्ले"
            "ru" -> "Шрифт и оформление чтения"
            "ar" -> "الخطوط والعرض للقارئ"
            "nl" -> "Typografie & Weergave"
            "id" -> "Tipografi & Tampilan Pembaca"
            "th" -> "การจัดพิมพ์และการแสดงผล"
            else -> "Reader Typography & Display"
        }
    }

    fun getCategoryTitle(category: String, langCode: String): String {
        val lang = langCode.lowercase().take(2)
        return when (category.lowercase()) {
            "all" -> when (lang) {
                "vi" -> "Tất cả"
                "es" -> "Todo"
                "fr" -> "Tout"
                "de" -> "Alle"
                "ja" -> "すべて"
                "ko" -> "전체"
                "zh" -> "全部"
                "pt" -> "Tudo"
                "it" -> "Tutti"
                "hi" -> "सभी"
                "ru" -> "Все"
                "ar" -> "الكل"
                "nl" -> "Alles"
                "id" -> "Semua"
                "th" -> "ทั้งหมด"
                else -> "All"
            }
            "technology" -> when (lang) {
                "vi" -> "Công nghệ"
                "es" -> "Tecnología"
                "fr" -> "Technologie"
                "de" -> "Technologie"
                "ja" -> "テクノロジー"
                "ko" -> "기술"
                "zh" -> "科技"
                "pt" -> "Tecnologia"
                "it" -> "Tecnologia"
                "hi" -> "प्रौद्योगिकी"
                "ru" -> "Технологии"
                "ar" -> "التكنولوجيا"
                "nl" -> "Technologie"
                "id" -> "Teknologi"
                "th" -> "เทคโนโลยี"
                else -> "Technology"
            }
            "science" -> when (lang) {
                "vi" -> "Khoa học"
                "es" -> "Ciencia"
                "fr" -> "Science"
                "de" -> "Wissenschaft"
                "ja" -> "科学"
                "ko" -> "과학"
                "zh" -> "科学"
                "pt" -> "Ciência"
                "it" -> "Scienza"
                "hi" -> "विज्ञान"
                "ru" -> "Наука"
                "ar" -> "العلوم"
                "nl" -> "Wetenschap"
                "id" -> "Sains"
                "th" -> "วิทยาศาสตร์"
                else -> "Science"
            }
            "world" -> when (lang) {
                "vi" -> "Thế giới"
                "es" -> "Mundo"
                "fr" -> "Monde"
                "de" -> "Welt"
                "ja" -> "国際"
                "ko" -> "세계"
                "zh" -> "国际"
                "pt" -> "Mundo"
                "it" -> "Mondo"
                "hi" -> "विश्व"
                "ru" -> "В мире"
                "ar" -> "العالم"
                "nl" -> "Wereld"
                "id" -> "Dunia"
                "th" -> "โลก"
                else -> "World"
            }
            "business" -> when (lang) {
                "vi" -> "Kinh doanh"
                "es" -> "Negocios"
                "fr" -> "Économie"
                "de" -> "Wirtschaft"
                "ja" -> "ビジネス"
                "ko" -> "비즈니스"
                "zh" -> "商业"
                "pt" -> "Negócios"
                "it" -> "Economia"
                "hi" -> "व्यापार"
                "ru" -> "Бизнес"
                "ar" -> "الأعمال"
                "nl" -> "Economie"
                "id" -> "Bisnis"
                "th" -> "ธุรกิจ"
                else -> "Business"
            }
            "entertainment" -> when (lang) {
                "vi" -> "Giải trí"
                "es" -> "Entretenimiento"
                "fr" -> "Divertissement"
                "de" -> "Unterhaltung"
                "ja" -> "エンタメ"
                "ko" -> "연예"
                "zh" -> "娱乐"
                "pt" -> "Entretenimento"
                "it" -> "Intrattenimento"
                "hi" -> "मनोरंजन"
                "ru" -> "Развлечения"
                "ar" -> "الترفيه"
                "nl" -> "Entertainment"
                "id" -> "Hiburan"
                "th" -> "บันเทิง"
                else -> "Entertainment"
            }
            "health" -> when (lang) {
                "vi" -> "Sức khỏe"
                "es" -> "Salud"
                "fr" -> "Santé"
                "de" -> "Gesundheit"
                "ja" -> "健康"
                "ko" -> "건강"
                "zh" -> "健康"
                "pt" -> "Saúde"
                "it" -> "Salute"
                "hi" -> "स्वास्थ्य"
                "ru" -> "Здоровье"
                "ar" -> "الصحة"
                "nl" -> "Gezondheid"
                "id" -> "Kesehatan"
                "th" -> "สุขภาพ"
                else -> "Health"
            }
            "sports" -> when (lang) {
                "vi" -> "Thể thao"
                "es" -> "Deportes"
                "fr" -> "Sports"
                "de" -> "Sport"
                "ja" -> "スポーツ"
                "ko" -> "스포츠"
                "zh" -> "体育"
                "pt" -> "Esportes"
                "it" -> "Sport"
                "hi" -> "खेल"
                "ru" -> "Спорт"
                "ar" -> "الرياضة"
                "nl" -> "Sport"
                "id" -> "Olahraga"
                "th" -> "กีฬา"
                else -> "Sports"
            }
            else -> category
        }
    }

    fun getBriefingTitle(langCode: String): String {
        return when (langCode.lowercase().take(2)) {
            "vi" -> "Bản tin tình báo hàng ngày"
            "es" -> "Resumen de Inteligencia Diario"
            "fr" -> "Briefing d'Intelligence Quotidien"
            "de" -> "Tägliches Intelligenz-Briefing"
            "ja" -> "毎日のインテリジェンス要約"
            "ko" -> "일일 인텔리전스 브리핑"
            "zh" -> "每日情报简报"
            "pt" -> "Briefing de Inteligência Diário"
            "it" -> "Briefing di Intelligence Giornaliero"
            "hi" -> "दैनिक इंटेलिजेंस ब्रीफिंग"
            "ru" -> "Ежедневная новостная сводка"
            "ar" -> "الملخص الإخباري اليومي"
            "nl" -> "Dagelijkse Intelligentie Briefing"
            "id" -> "Ringkasan Intelijen Harian"
            "th" -> "สรุปข่าวกรองรายวัน"
            else -> "Daily Intelligence Briefing"
        }
    }

    fun getBriefingSubtitle(langCode: String): String {
        return when (langCode.lowercase().take(2)) {
            "vi" -> "Tổng hợp tin tức quan trọng nhất từ các nguồn uy tín cao..."
            "es" -> "Compilando el resumen diario de máxima prioridad..."
            "fr" -> "Compilation du briefing quotidien prioritaire..."
            "de" -> "Zusammenstellung des täglichen Briefings hoher Priorität..."
            "ja" -> "高シグナル源からの毎日の優先インテリジェンスをまとめ中..."
            "ko" -> "고신호 소스에서 일일 최우선 인텔리전스 취합 중..."
            "zh" -> "正在汇总来自高信噪比来源的每日高优先级情报..."
            "pt" -> "Compilando o briefing diário de alta prioridade..."
            "it" -> "Compilazione del briefing giornaliero prioritario..."
            "hi" -> "उच्च-SNR स्रोतों से दैनिक प्राथमिकता ब्रीफिंग संकलित की जा रही है..."
            "ru" -> "Формирование ежедневной сводки из приоритетных источников..."
            "ar" -> "تجميع الملخص اليومي عالي الأهمية من المصادر الموثوقة..."
            "nl" -> "Verzamelen van de dagelijkse top-prioriteit briefing..."
            "id" -> "Menyusun ringkasan harian prioritas tinggi dari sumber utama..."
            "th" -> "กำลังรวบรวมสรุปข่าวกรองสำคัญรายวัน..."
            else -> "Compiling daily top-priority intelligence briefing from high-SNR sources..."
        }
    }

    fun getListenText(langCode: String): String {
        return when (langCode.lowercase().take(2)) {
            "vi" -> "Nghe"
            "es" -> "Escuchar"
            "fr" -> "Écouter"
            "de" -> "Anhören"
            "ja" -> "聴く"
            "ko" -> "듣기"
            "zh" -> "收听"
            "pt" -> "Ouvir"
            "it" -> "Ascolta"
            "hi" -> "सुनें"
            "ru" -> "Слушать"
            "ar" -> "استماع"
            "nl" -> "Luisteren"
            "id" -> "Dengar"
            "th" -> "ฟัง"
            else -> "Listen"
        }
    }

    fun getViewRawHeadline(langCode: String): String {
        return when (langCode.lowercase().take(2)) {
            "vi" -> "Xem tiêu đề gốc ˅"
            "es" -> "Ver titular original ˅"
            "fr" -> "Voir le titre original ˅"
            "de" -> "Originalschlagzeile anzeigen ˅"
            "ja" -> "元の見出しを表示 ˅"
            "ko" -> "원문 헤드라인 보기 ˅"
            "zh" -> "查看原始标题 ˅"
            "pt" -> "Ver manchete original ˅"
            "it" -> "Vedi titolo originale ˅"
            "hi" -> "मूल सुर्खियां देखें ˅"
            "ru" -> "Показать исходный заголовок ˅"
            "ar" -> "عرض العنوان الأصلي ˅"
            "nl" -> "Bekijk originele kop ˅"
            "id" -> "Lihat judul asli ˅"
            "th" -> "ดูหัวข้อข่าวต้นฉบับ ˅"
            else -> "View raw headline ˅"
        }
    }

    fun getHideRawHeadline(langCode: String): String {
        return when (langCode.lowercase().take(2)) {
            "vi" -> "Ẩn tiêu đề gốc ˄"
            "es" -> "Ocultar titular original ˄"
            "fr" -> "Masquer le titre original ˄"
            "de" -> "Originalschlagzeile ausblenden ˄"
            "ja" -> "元の見出しを非表示 ˄"
            "ko" -> "원문 헤드라인 숨기기 ˄"
            "zh" -> "隐藏原始标题 ˄"
            "pt" -> "Ocultar manchete original ˄"
            "it" -> "Nascondi titolo originale ˄"
            "hi" -> "मूल सुर्खियां छिपाएं ˄"
            "ru" -> "Скрыть исходный заголовок ˄"
            "ar" -> "إخفاء العنوان الأصلي ˄"
            "nl" -> "Verberg originele kop ˄"
            "id" -> "Sembunyikan judul asli ˄"
            "th" -> "ซ่อนหัวข้อข่าวต้นฉบับ ˄"
            else -> "Hide raw headline ˄"
        }
    }
}
