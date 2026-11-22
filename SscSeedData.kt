package com.example.data.repository

import com.example.data.local.entity.SscChapterEntity
import com.example.data.local.entity.SscSubjectEntity

data class SscSeedSubject(
  val nameEn: String,
  val nameBn: String,
  val accentIndex: Int,
  val chapters: List<Pair<String, String>>,
)

object SscSeedData {
  val subjects =
    listOf(
      SscSeedSubject(
        nameEn = "Physics",
        nameBn = "পদার্থবিজ্ঞান",
        accentIndex = 0,
        chapters =
          listOf(
            "Ch 1: Physical Quantities & Measurement" to "অধ্যায় ১: ভৌত রাশি ও পরিমাপ",
            "Ch 2: Motion" to "অধ্যায় ২: গতি",
            "Ch 3: Force" to "অধ্যায় ৩: বল",
            "Ch 4: Work, Power & Energy" to "অধ্যায় ৪: কাজ, ক্ষমতা ও শক্তি",
            "Ch 5: State of Matter & Pressure" to "অধ্যায় ৫: পদার্থের অবস্থা ও চাপ",
            "Ch 6: Effect of Heat on Matter" to "অধ্যায় ৬: বস্তুর উপর তাপের প্রভাব",
            "Ch 7: Waves & Sound" to "অধ্যায় ৭: তরঙ্গ ও শব্দ",
            "Ch 8: Reflection of Light" to "অধ্যায় ৮: আলোর প্রতিফলন",
            "Ch 9: Refraction of Light" to "অধ্যায় ৯: আলোর প্রতিসরণ",
            "Ch 10: Static Electricity" to "অধ্যায় ১০: স্থির তড়িৎ",
            "Ch 11: Current Electricity" to "অধ্যায় ১১: চল তড়িৎ",
            "Ch 12: Magnetic Effect of Current" to "অধ্যায় ১২: তড়িতের চৌম্বক ক্রিয়া",
          ),
      ),
      SscSeedSubject(
        nameEn = "Chemistry",
        nameBn = "রসায়ন",
        accentIndex = 1,
        chapters =
          listOf(
            "Ch 1: Concept of Chemistry" to "অধ্যায় ১: রসায়নের ধারণা",
            "Ch 2: States of Matter" to "অধ্যায় ২: পদার্থের অবস্থা",
            "Ch 3: Structure of Matter" to "অধ্যায় ৩: পদার্থের গঠন",
            "Ch 4: Periodic Table" to "অধ্যায় ৪: পর্যায় সারণি",
            "Ch 5: Chemical Bond" to "অধ্যায় ৫: রাসায়নিক বন্ধন",
            "Ch 6: Concept of Mole & Counting" to "অধ্যায় ৬: মোলের ধারণা ও গণনা",
            "Ch 7: Chemical Reactions" to "অধ্যায় ৭: রাসায়নিক বিক্রিয়া",
            "Ch 8: Chemistry & Energy" to "অধ্যায় ৮: রসায়ন ও শক্তি",
            "Ch 9: Acid-Base Balance" to "অধ্যায় ৯: এসিড-ক্ষারক সমতা",
            "Ch 10: Mineral Resources: Metals" to "অধ্যায় ১০: খনিজ সম্পদ: ধাতু-অধাতু",
            "Ch 11: Mineral Resources: Fossils" to "অধ্যায় ১১: খনিজ সম্পদ: জীবাশ্ম",
          ),
      ),
      SscSeedSubject(
        nameEn = "General Math",
        nameBn = "সাধারণ গণিত",
        accentIndex = 2,
        chapters =
          listOf(
            "Ch 1: Real Numbers" to "অধ্যায় ১: বাস্তব সংখ্যা",
            "Ch 2: Sets & Functions" to "অধ্যায় ২: সেট ও ফাংশন",
            "Ch 3: Algebraic Expressions" to "অধ্যায় ৩: বীজগাণিতিক রাশি",
            "Ch 4: Exponents & Logarithms" to "অধ্যায় ৪: সূচক ও লগারিদম",
            "Ch 5: Equations in One Variable" to "অধ্যায় ৫: এক চলকবিশিষ্ট সমীকরণ",
            "Ch 6: Lines, Angles & Triangles" to "অধ্যায় ৬: রেখা, কোণ ও ত্রিভুজ",
            "Ch 8: Circles" to "অধ্যায় ৮: বৃত্ত",
            "Ch 9: Trigonometric Ratio" to "অধ্যায় ৯: ত্রিকোণমিতিক অনুপাত",
            "Ch 11: Algebraic Ratio & Proportion" to "অধ্যায় ১১: বীজগাণিতিক অনুপাত",
            "Ch 13: Finite Series" to "অধ্যায় ১৩: সসীম ধারা",
            "Ch 16: Mensuration" to "অধ্যায় ১৬: পরিমিতি",
            "Ch 17: Statistics" to "অধ্যায় ১৭: পরিসংখ্যান",
          ),
      ),
      SscSeedSubject(
        nameEn = "Higher Math",
        nameBn = "উচ্চতর গণিত",
        accentIndex = 3,
        chapters =
          listOf(
            "Ch 1: Set & Function" to "অধ্যায় ১: সেট ও ফাংশন",
            "Ch 2: Algebraic Expressions" to "অধ্যায় ২: বীজগাণিতিক রাশি",
            "Ch 3: Geometry Theorems" to "অধ্যায় ৩: জ্যামিতি",
            "Ch 5: Quadratic Equations" to "অধ্যায় ৫: সমীকরণ",
            "Ch 7: Infinite Series" to "অধ্যায় ৭: অসীম ধারা",
            "Ch 8: Trigonometry" to "অধ্যায় ৮: ত্রিকোণমিতি",
            "Ch 9: Exponential & Log Functions" to "অধ্যায় ৯: সূচকীয় ও লগারিদমীয় ফাংশন",
            "Ch 10: Binomial Expansion" to "অধ্যায় ১০: দ্বিপদী বিস্তৃতি",
            "Ch 11: Coordinate Geometry" to "অধ্যায় ১১: স্থানাঙ্ক জ্যামিতি",
            "Ch 14: Probability" to "অধ্যায় ১৪: সম্ভাবনা",
          ),
      ),
      SscSeedSubject(
        nameEn = "Biology",
        nameBn = "জীববিজ্ঞান",
        accentIndex = 0,
        chapters =
          listOf(
            "Ch 1: Lessons on Life" to "অধ্যায় ১: জীবন পাঠ",
            "Ch 2: Cells & Tissues" to "অধ্যায় ২: জীবকোষ ও টিস্যু",
            "Ch 3: Cell Division" to "অধ্যায় ৩: কোষ বিভাজন",
            "Ch 4: Bioenergetics" to "অধ্যায় ৪: জীবনীশক্তি",
            "Ch 5: Food, Nutrition & Digestion" to "অধ্যায় ৫: খাদ্য, পুষ্টি ও পরিপাক",
            "Ch 6: Transport in Organisms" to "অধ্যায় ৬: জীবে পরিবহন",
            "Ch 11: Reproduction in Organisms" to "অধ্যায় ১১: জীবের প্রজনন",
            "Ch 12: Heredity & Evolution" to "অধ্যায় ১২: জীবের বংশগতি ও বিবর্তন",
          ),
      ),
      SscSeedSubject(
        nameEn = "English",
        nameBn = "ইংরেজি",
        accentIndex = 1,
        chapters =
          listOf(
            "Reading Comprehension & Seen Passages" to "রিডিং কম্প্রিহেনশন ও প্যাসেজ",
            "Right Form of Verbs & Tense" to "রাইট ফর্ম অব ভার্বস ও টেন্স",
            "Transformation & Narration" to "ট্রান্সফরমেশন ও ন্যারেশন",
            "Paragraph, Email & Composition" to "প্যারাগ্রাফ, ইমেইল ও রচনা",
            "Completing Sentences & Connectors" to "কমপ্লিটিং সেন্টেন্স ও কানেক্টর",
          ),
      ),
      SscSeedSubject(
        nameEn = "Bangla",
        nameBn = "বাংলা",
        accentIndex = 2,
        chapters =
          listOf(
            "গদ্য (Prose Chapters)" to "বাংলা ১ম পত্র: গদ্য পাঠ",
            "পদ্য (Poetry Chapters)" to "বাংলা ১ম পত্র: কবিতা পাঠ",
            "সহপাঠ: উপন্যাস ও নাটক" to "সহপাঠ: কাকতাড়ুয়া ও বহিপীর",
            "বাংলা ব্যাকরণ: ধ্বনি, সন্ধি ও সমাস" to "বাংলা ২য় পত্র: ব্যাকরণ অংশ",
            "ভাবসম্প্রসারণ, চিঠি ও রচনা" to "বাংলা ২য় পত্র: নির্মিতি অংশ",
          ),
      ),
      SscSeedSubject(
        nameEn = "ICT",
        nameBn = "তথ্য ও যোগাযোগ প্রযুক্তি",
        accentIndex = 3,
        chapters =
          listOf(
            "Ch 1: ICT & Our Bangladesh" to "অধ্যায় ১: তথ্য ও যোগাযোগ প্রযুক্তি এবং আমাদের বাংলাদেশ",
            "Ch 2: Computer & User Security" to "অধ্যায় ২: কম্পিউটার ও কম্পিউটার ব্যবহারকারীর নিরাপত্তা",
            "Ch 3: Internet in My Education" to "অধ্যায় ৩: আমার শিক্ষায় ইন্টারনেট",
            "Ch 4: Writing & Calculations" to "অধ্যায় ৪: আমার লেখালেখি ও হিসাব",
            "Ch 5: Multimedia & Graphics" to "অধ্যায় ৫: মাল্টিমিডিয়া ও গ্রাফিক্স",
            "Ch 6: Database & Its Uses" to "অধ্যায় ৬: ডেটাবেজ এর ব্যবহার",
          ),
      ),
    )

  fun toSubjectEntity(seed: SscSeedSubject): SscSubjectEntity =
    SscSubjectEntity(nameEn = seed.nameEn, nameBn = seed.nameBn, accentIndex = seed.accentIndex)

  fun toChapterEntities(subjectId: Long, seed: SscSeedSubject): List<SscChapterEntity> =
    seed.chapters.mapIndexed { idx, pair ->
      SscChapterEntity(
        subjectId = subjectId,
        titleEn = pair.first,
        titleBn = pair.second,
        isCompleted = false,
        chapterOrder = idx,
      )
    }
}
