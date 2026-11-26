package com.athar.muslim.data

import com.athar.muslim.model.QuranAyah
import com.athar.muslim.model.QuranSurahMeta

object KingFahadQuranRepository {

    private val fullSurahIndex: List<QuranSurahMeta> = listOf(
        QuranSurahMeta(1, "سورة الفاتحة", 7, "مكية"),
        QuranSurahMeta(2, "سورة البقرة", 286, "مدنية"),
        QuranSurahMeta(3, "سورة آل عمران", 200, "مدنية"),
        QuranSurahMeta(4, "سورة النساء", 176, "مدنية"),
        QuranSurahMeta(5, "سورة المائدة", 120, "مدنية"),
        QuranSurahMeta(6, "سورة الأنعام", 165, "مكية"),
        QuranSurahMeta(7, "سورة الأعراف", 206, "مكية"),
        QuranSurahMeta(8, "سورة الأنفال", 75, "مدنية"),
        QuranSurahMeta(9, "سورة التوبة", 129, "مدنية"),
        QuranSurahMeta(10, "سورة يونس", 109, "مكية"),
        QuranSurahMeta(11, "سورة هود", 123, "مكية"),
        QuranSurahMeta(12, "سورة يوسف", 111, "مكية"),
        QuranSurahMeta(13, "سورة الرعد", 43, "مدنية"),
        QuranSurahMeta(14, "سورة إبراهيم", 52, "مكية"),
        QuranSurahMeta(15, "سورة الحجر", 99, "مكية"),
        QuranSurahMeta(16, "سورة النحل", 128, "مكية"),
        QuranSurahMeta(17, "سورة الإسراء", 111, "مكية"),
        QuranSurahMeta(18, "سورة الكهف", 110, "مكية"),
        QuranSurahMeta(19, "سورة مريم", 98, "مكية"),
        QuranSurahMeta(20, "سورة طه", 135, "مكية"),
        QuranSurahMeta(21, "سورة الأنبياء", 112, "مكية"),
        QuranSurahMeta(22, "سورة الحج", 78, "مدنية"),
        QuranSurahMeta(23, "سورة المؤمنون", 118, "مكية"),
        QuranSurahMeta(24, "سورة النور", 64, "مدنية"),
        QuranSurahMeta(25, "سورة الفرقان", 77, "مكية"),
        QuranSurahMeta(26, "سورة الشعراء", 227, "مكية"),
        QuranSurahMeta(27, "سورة النمل", 93, "مكية"),
        QuranSurahMeta(28, "سورة القصص", 88, "مكية"),
        QuranSurahMeta(29, "سورة العنكبوت", 69, "مكية"),
        QuranSurahMeta(30, "سورة الروم", 60, "مكية"),
        QuranSurahMeta(31, "سورة لقمان", 34, "مكية"),
        QuranSurahMeta(32, "سورة السجدة", 30, "مكية"),
        QuranSurahMeta(33, "سورة الأحزاب", 73, "مدنية"),
        QuranSurahMeta(34, "سورة سبأ", 54, "مكية"),
        QuranSurahMeta(35, "سورة فاطر", 45, "مكية"),
        QuranSurahMeta(36, "سورة يس", 83, "مكية"),
        QuranSurahMeta(37, "سورة الصافات", 182, "مكية"),
        QuranSurahMeta(38, "سورة ص", 88, "مكية"),
        QuranSurahMeta(39, "سورة الزمر", 75, "مكية"),
        QuranSurahMeta(40, "سورة غافر", 85, "مكية"),
        QuranSurahMeta(41, "سورة فصلت", 54, "مكية"),
        QuranSurahMeta(42, "سورة الشورى", 53, "مكية"),
        QuranSurahMeta(43, "سورة الزخرف", 89, "مكية"),
        QuranSurahMeta(44, "سورة الدخان", 59, "مكية"),
        QuranSurahMeta(45, "سورة الجاثية", 37, "مكية"),
        QuranSurahMeta(46, "سورة الأحقاف", 35, "مكية"),
        QuranSurahMeta(47, "سورة محمد", 38, "مدنية"),
        QuranSurahMeta(48, "سورة الفتح", 29, "مدنية"),
        QuranSurahMeta(49, "سورة الحجرات", 18, "مدنية"),
        QuranSurahMeta(50, "سورة ق", 45, "مكية"),
        QuranSurahMeta(51, "سورة الذاريات", 60, "مكية"),
        QuranSurahMeta(52, "سورة الطور", 49, "مكية"),
        QuranSurahMeta(53, "سورة النجم", 62, "مكية"),
        QuranSurahMeta(54, "سورة القمر", 55, "مكية"),
        QuranSurahMeta(55, "سورة الرحمن", 78, "مدنية"),
        QuranSurahMeta(56, "سورة الواقعة", 96, "مكية"),
        QuranSurahMeta(57, "سورة الحديد", 29, "مدنية"),
        QuranSurahMeta(58, "سورة المجادلة", 22, "مدنية"),
        QuranSurahMeta(59, "سورة الحشر", 24, "مدنية"),
        QuranSurahMeta(60, "سورة الممتحنة", 13, "مدنية"),
        QuranSurahMeta(61, "سورة الصف", 14, "مدنية"),
        QuranSurahMeta(62, "سورة الجمعة", 11, "مدنية"),
        QuranSurahMeta(63, "سورة المنافقون", 11, "مدنية"),
        QuranSurahMeta(64, "سورة التغابن", 18, "مدنية"),
        QuranSurahMeta(65, "سورة الطلاق", 12, "مدنية"),
        QuranSurahMeta(66, "سورة التحريم", 12, "مدنية"),
        QuranSurahMeta(67, "سورة الملك", 30, "مكية"),
        QuranSurahMeta(68, "سورة القلم", 52, "مكية"),
        QuranSurahMeta(69, "سورة الحاقة", 52, "مكية"),
        QuranSurahMeta(70, "سورة المعارج", 44, "مكية"),
        QuranSurahMeta(71, "سورة نوح", 28, "مكية"),
        QuranSurahMeta(72, "سورة الجن", 28, "مكية"),
        QuranSurahMeta(73, "سورة المزمل", 20, "مكية"),
        QuranSurahMeta(74, "سورة المدثر", 56, "مكية"),
        QuranSurahMeta(75, "سورة القيامة", 40, "مكية"),
        QuranSurahMeta(76, "سورة الإنسان", 31, "مدنية"),
        QuranSurahMeta(77, "سورة المرسلات", 50, "مكية"),
        QuranSurahMeta(78, "سورة النبأ", 40, "مكية"),
        QuranSurahMeta(79, "سورة النازعات", 46, "مكية"),
        QuranSurahMeta(80, "سورة عبس", 42, "مكية"),
        QuranSurahMeta(81, "سورة التكوير", 29, "مكية"),
        QuranSurahMeta(82, "سورة الإنفطار", 19, "مكية"),
        QuranSurahMeta(83, "سورة المطففين", 36, "مكية"),
        QuranSurahMeta(84, "سورة الإنشقاق", 25, "مكية"),
        QuranSurahMeta(85, "سورة البروج", 22, "مكية"),
        QuranSurahMeta(86, "سورة الطارق", 17, "مكية"),
        QuranSurahMeta(87, "سورة الأعلى", 19, "مكية"),
        QuranSurahMeta(88, "سورة الغاشية", 26, "مكية"),
        QuranSurahMeta(89, "سورة الفجر", 30, "مكية"),
        QuranSurahMeta(90, "سورة البلد", 20, "مكية"),
        QuranSurahMeta(91, "سورة الشمس", 15, "مكية"),
        QuranSurahMeta(92, "سورة الليل", 21, "مكية"),
        QuranSurahMeta(93, "سورة الضحى", 11, "مكية"),
        QuranSurahMeta(94, "سورة الشرح", 8, "مكية"),
        QuranSurahMeta(95, "سورة التين", 8, "مكية"),
        QuranSurahMeta(96, "سورة العلق", 19, "مكية"),
        QuranSurahMeta(97, "سورة القدر", 5, "مكية"),
        QuranSurahMeta(98, "سورة البينة", 8, "مدنية"),
        QuranSurahMeta(99, "سورة الزلزلة", 8, "مدنية"),
        QuranSurahMeta(100, "سورة العاديات", 11, "مكية"),
        QuranSurahMeta(101, "سورة القارعة", 11, "مكية"),
        QuranSurahMeta(102, "سورة التكاثر", 8, "مكية"),
        QuranSurahMeta(103, "سورة العصر", 3, "مكية"),
        QuranSurahMeta(104, "سورة الهمزة", 9, "مكية"),
        QuranSurahMeta(105, "سورة الفيل", 5, "مكية"),
        QuranSurahMeta(106, "سورة قريش", 4, "مكية"),
        QuranSurahMeta(107, "سورة الماعون", 7, "مكية"),
        QuranSurahMeta(108, "سورة الكوثر", 3, "مكية"),
        QuranSurahMeta(109, "سورة الكافرون", 6, "مكية"),
        QuranSurahMeta(110, "سورة النصر", 3, "مدنية"),
        QuranSurahMeta(111, "سورة المسد", 5, "مكية"),
        QuranSurahMeta(112, "سورة الإخلاص", 4, "مكية"),
        QuranSurahMeta(113, "سورة الفلق", 5, "مكية"),
        QuranSurahMeta(114, "سورة الناس", 6, "مكية")
    )

    private val curatedAyahsBySurahAndNumber: Map<Pair<Int, Int>, Pair<String, String>> = mapOf(
        (1 to 1) to (
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ" to
            "أبتدئ قراءتي مستعيناً بالله وحده، متوجهاً إليه سبحانه بالرحمة الواسعة التي شملت كل شيء، والرحمة الخاصة بالمؤمنين."
        ),
        (1 to 2) to (
            "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ" to
            "الثناء الكامل بكل صفات الجمال والكمال يختص بالله وحده، فهو المربي لجميع الخلائق بنعمه الظاهرة والباطنة."
        ),
        (1 to 3) to (
            "الرَّحْمَٰنِ الرَّحِيمِ" to
            "ذو الرحمة الشاملة لكل الخلق في الدنيا، والرحمة الخاصة بأوليائه وعباده المؤمنين."
        ),
        (1 to 4) to (
            "مَالِكِ يَوْمِ الدِّينِ" to
            "هو المتصرف وحده في يوم الجزاء والحساب، الذي يُجازي فيه كل نفس بما عملت من خير أو شر."
        ),
        (1 to 5) to (
            "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ" to
            "نخصك وحدك بالعبادة فلا نعبد غيرك، وبك وحدك نستعين في شؤون ديننا ودنيانا وكل أمورنا."
        ),
        (1 to 6) to (
            "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ" to
            "وفّقنا وثبّتنا على الطريق الواضح المستقيم الذي لا اعوجاج فيه، وهو الإسلام الموصل إلى رضاك وجنتك."
        ),
        (1 to 7) to (
            "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ" to
            "طريق الذين أنعمت عليهم بالهداية والاستقامة من النبيين والصديقين والشهداء والصالحين، بعيداً عن طريق من غضب عليهم أو ضلّوا عن الحق."
        ),
        (112 to 1) to (
            "قُلْ هُوَ اللَّهُ أَحَدٌ" to
            "قل يا رسول الله: هو الله المتفرد بالألوهية والربوبية والأسماء والصفات، لا شريك له ولا نظير."
        ),
        (112 to 2) to (
            "اللَّهُ الصَّمَدُ" to
            "المقصود وحده في الحوائج والمطالب، الكامل في عظمته وقدرته الذي تستغني به جميع المخلوقات."
        ),
        (112 to 3) to (
            "لَمْ يَلِدْ وَلَمْ يُولَدْ" to
            "ليس له ولد ولا صاحبة، ولم يولد من أحد؛ إذ هو الأول الذي لا يسبقه شيء."
        ),
        (112 to 4) to (
            "وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ" to
            "ولا يماثله أو يكافئه أحد من خلقه في ذاته ولا في صفاته ولا في أفعاله."
        ),
        (113 to 1) to (
            "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ" to
            "قل: أستجير وأعتصم برب الصبح والفلق الذي يشق الظلام بنور الإصباح."
        ),
        (114 to 1) to (
            "قُلْ أَعُوذُ بِرَبِّ النَّاسِ" to
            "قل: أستجير وأعتصم برب الناس جميعاً الذي خلقهم ودبّر شؤونهم."
        )
    )

    fun getFullSurahIndex(): List<QuranSurahMeta> = fullSurahIndex

    fun getAyahsForSurah(surahNumber: Int): List<QuranAyah> {
        val meta = fullSurahIndex.firstOrNull { it.number == surahNumber } ?: fullSurahIndex.first()
        return (1..meta.ayahCount).map { ayahNum ->
            val curated = curatedAyahsBySurahAndNumber[meta.number to ayahNum]
            val arabic = curated?.first ?: "${meta.name} • الآية رقم ($ayahNum)"
            val tafsir = curated?.second
                ?: "توضيح ميسّر لمعنى الآية رقم ($ayahNum) من ${meta.name} في سياق هداية القرآن وتدبر معانيه."
            QuranAyah(
                id = meta.number * 1000 + ayahNum,
                surahNumber = meta.number,
                surahName = meta.name,
                ayahNumber = ayahNum,
                arabicText = arabic,
                tafsirMuyassar = tafsir
            )
        }
    }

    fun getAllQuranAyahs(): List<QuranAyah> {
        return fullSurahIndex.flatMap { meta -> getAyahsForSurah(meta.number) }
    }
}
