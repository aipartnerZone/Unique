package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LoadedPdfDocumentState(
    val uriString: String = "",
    val fileName: String = "",
    val currentPageIndex: Int = 0,
    val totalPages: Int = 0,
    val currentPageBitmap: Bitmap? = null,
    val extractedOrAnalyzedText: String = "",
    val isSampleDocument: Boolean = false,
    val isAnalyzingWithAi: Boolean = false
)

data class SampleStudyDocument(
    val id: String,
    val title: String,
    val subjectBadge: String,
    val contentPages: List<String>
)

object MunnaPdfHelper {

    private const val TAG = "MunnaPdfHelper"

    val sampleStudyDocuments = listOf(
        SampleStudyDocument(
            id = "science_notes",
            title = "विज्ञान नोट्स: प्रकाश संश्लेषण और न्यूटन के नियम",
            subjectBadge = "कक्षा 9–12 विज्ञान (Science)",
            contentPages = listOf(
                """
                अध्याय 1: प्रकाश संश्लेषण (Photosynthesis)
                • परिभाषा: हरे पौधे सूर्य के प्रकाश, क्लोरोफिल (Chlorophyll), कार्बन डाइऑक्साइड (CO₂) और जल (H₂O) की मदद से अपना भोजन (ग्लूकोज़) स्वयं बनाते हैं।
                • रासायनिक समीकरण: 6CO₂ + 6H₂O + सूर्य का प्रकाश → C₆H₁₂O₆ (ग्लूकोज़) + 6O₂ (ऑक्सीजन)।
                • महत्व: इस प्रक्रिया से पृथ्वी पर जीवन के लिए आवश्यक ऑक्सीजन गैस मुक्त होती है।
                """.trimIndent(),
                """
                अध्याय 2: न्यूटन के गति के तीन नियम (Newton's Laws of Motion)
                1. प्रथम नियम (जड़त्व का नियम / Law of Inertia): यदि कोई वस्तु स्थिर है तो वह स्थिर ही रहेगी, और गतिमान है तो उसी वेग से चलती रहेगी जब तक उस पर कोई बाहरी बल न लगाया जाए।
                2. द्वितीय नियम (F = m × a): किसी वस्तु पर लगने वाला बल उसके द्रव्यमान (m) और त्वरण (a) के गुणनफल के बराबर होता है।
                3. तृतीय नियम (क्रिया-प्रतिक्रिया / Action-Reaction): प्रत्येक क्रिया के बराबर और विपरीत दिशा में प्रतिक्रिया होती है (जैसे रॉकेट का ऊपर उड़ना)।
                """.trimIndent()
            )
        ),
        SampleStudyDocument(
            id = "ai_computer_notes",
            title = "कंप्यूटर और AI: आर्टिफिशियल इंटेलिजेंस की समझ",
            subjectBadge = "टेक्नोलॉजी और कोडिंग (Tech & AI)",
            contentPages = listOf(
                """
                आर्टिफिशियल इंटेलिजेंस (Artificial Intelligence - AI) क्या है?
                • जब मशीनों और कंप्यूटर प्रोग्राम को इस तरह बनाया जाता है कि वे इंसानों की तरह सोच सकें, डेटा से सीख सकें (Machine Learning) और समस्याओं का समाधान कर सकें, तो उसे AI कहते हैं।
                • एजेंट (AI Agent) कैसे काम करता है?
                  1. सुनना और समझना (Speech & Natural Language Understanding)
                  2. यूज़र के डेटा और आदतों से सीखना (Memory & Personalization)
                  3. काम पूरा करना (जैसे ऐप्स खोलना, कॉल लगाना, PDF पढ़ना और सवालों का जवाब देना)।
                """.trimIndent()
            )
        ),
        SampleStudyDocument(
            id = "gk_constitution_notes",
            title = "सामान्य ज्ञान: भारतीय संविधान और मौलिक अधिकार",
            subjectBadge = "बोर्ड और प्रतियोगी परीक्षा (GK / Civics)",
            contentPages = listOf(
                """
                भारतीय संविधान के मुख्य बिंदु (Indian Constitution Key Notes):
                • संविधान सभा के प्रारूप समिति (Drafting Committee) के अध्यक्ष: डॉ. भीमराव अंबेडकर।
                • संविधान लागू होने की तिथि: 26 जनवरी 1950 (गणतंत्र दिवस)।
                • नागरिकों के 6 मौलिक अधिकार (Fundamental Rights - भाग 3):
                  1. समानता का अधिकार (अनुच्छेद 14–18)
                  2. स्वतंत्रता का अधिकार (अनुच्छेद 19–22)
                  3. शोषण के विरुद्ध अधिकार (अनुच्छेद 23–24)
                  4. धार्मिक स्वतंत्रता का अधिकार (अनुच्छेद 25–28)
                  5. संस्कृति और शिक्षा संबंधी अधिकार (अनुच्छेद 29–30)
                  6. संवैधानिक उपचारों का अधिकार (अनुच्छेद 32)।
                """.trimIndent()
            )
        )
    )

    fun resolveFileName(context: Context, uri: Uri): String {
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    val name = cursor.getString(nameIndex)
                    if (!name.isNullOrBlank()) return name
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to query PDF display name", e)
        }
        return uri.lastPathSegment ?: "Document.pdf"
    }

    /**
     * Renders a specific page of a PDF URI into a clean white-backed Bitmap using Android's PdfRenderer.
     * Returns Pair(RenderedBitmap, TotalPageCount).
     */
    suspend fun renderPdfPageBitmap(
        context: Context,
        uri: Uri,
        pageIndex: Int
    ): Pair<Bitmap?, Int> = withContext(Dispatchers.IO) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext (null to 0)
            pfd.use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    val count = renderer.pageCount
                    if (count <= 0) return@withContext (null to 0)
                    val safeIndex = pageIndex.coerceIn(0, count - 1)
                    renderer.openPage(safeIndex).use { page ->
                        val targetWidth = 1080
                        val scale = targetWidth.toFloat() / page.width.coerceAtLeast(1)
                        val targetHeight = (page.height * scale).toInt().coerceIn(400, 2200)
                        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        return@withContext (bitmap to count)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to render PDF page $pageIndex from $uri", e)
            return@withContext (null to 0)
        }
    }
}
