package com.example

import com.example.data.local.MunnaAgentMode
import com.example.domain.parser.MunnaCommandParser
import com.example.domain.parser.ParsedCommand
import com.example.domain.parser.TargetApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun parse_youtubeKholo_recognizesYouTubeOpenCommand() {
        val cmd1 = MunnaCommandParser.parse("Munna, YouTube kholo.")
        assertTrue(cmd1 is ParsedCommand.OpenApp)
        assertEquals(TargetApp.YOUTUBE, (cmd1 as ParsedCommand.OpenApp).targetApp)

        val cmd2 = MunnaCommandParser.parse("मुन्ना भाई यूट्यूब खोलो")
        assertTrue(cmd2 is ParsedCommand.OpenApp)
        assertEquals(TargetApp.YOUTUBE, (cmd2 as ParsedCommand.OpenApp).targetApp)
    }

    @Test
    fun parse_whatsappAndChromeAndFiles_recognizesTargetApps() {
        val wa = MunnaCommandParser.parse("WhatsApp kholo.")
        assertTrue(wa is ParsedCommand.OpenApp)
        assertEquals(TargetApp.WHATSAPP, (wa as ParsedCommand.OpenApp).targetApp)

        val chrome = MunnaCommandParser.parse("Chrome open karo.")
        assertTrue(chrome is ParsedCommand.OpenApp)
        assertEquals(TargetApp.CHROME, (chrome as ParsedCommand.OpenApp).targetApp)

        val files = MunnaCommandParser.parse("Meri files kholo.")
        assertTrue(files is ParsedCommand.OpenApp)
        assertEquals(TargetApp.FILES, (files as ParsedCommand.OpenApp).targetApp)
    }

    @Test
    fun parse_paymentApps_areStrictlyBlockedForSecurity() {
        val gpay = MunnaCommandParser.parse("Munna, Google Pay kholo")
        assertTrue(gpay is ParsedCommand.BlockedPaymentApp)

        val phonepe = MunnaCommandParser.parse("PhonePe open karo")
        assertTrue(phonepe is ParsedCommand.BlockedPaymentApp)

        val paytm = MunnaCommandParser.parse("पेटीएम खोलो")
        assertTrue(paytm is ParsedCommand.BlockedPaymentApp)

        val bhim = MunnaCommandParser.parse("BHIM UPI kholo")
        assertTrue(bhim is ParsedCommand.BlockedPaymentApp)
    }

    @Test
    fun parse_voiceCallsAndPdfAndAgentModes_recognizesCorrectly() {
        val callCmd = MunnaCommandParser.parse("Munna, 9876543210 par call karo")
        assertTrue(callCmd is ParsedCommand.MakePhoneCall)
        assertEquals("9876543210", (callCmd as ParsedCommand.MakePhoneCall).extractedDigits)

        val pdfCmd = MunnaCommandParser.parse("Munna, PDF padho")
        assertTrue(pdfCmd is ParsedCommand.OpenPdfReader)

        val discussCmd = MunnaCommandParser.parse("Munna, mujhse discuss karo")
        assertTrue(discussCmd is ParsedCommand.SwitchAgentMode)
        assertEquals(MunnaAgentMode.DISCUSSION_PARTNER, (discussCmd as ParsedCommand.SwitchAgentMode).targetMode)

        val studyCmd = MunnaCommandParser.parse("Mere sath padhai karo")
        assertTrue(studyCmd is ParsedCommand.SwitchAgentMode)
        assertEquals(MunnaAgentMode.STUDENT_TUTOR, (studyCmd as ParsedCommand.SwitchAgentMode).targetMode)
    }

    @Test
    fun extractLearnedMemory_learnsUserNameAndContactNumber() {
        val nameFact = MunnaCommandParser.extractLearnedMemoryIfAny("Mera naam Rahul hai")
        assertNotNull(nameFact)
        assertEquals("user_name", nameFact?.memoryKey)

        val contactFact = MunnaCommandParser.extractLearnedMemoryIfAny("Papa ka number 9876543210 hai")
        assertNotNull(contactFact)
        assertEquals("9876543210", contactFact?.contactPhone)
    }

    @Test
    fun parse_extendedSystemAppsAndCustomAppNames_recognizesCorrectly() {
        val cam = MunnaCommandParser.parse("Munna, Camera kholo.")
        assertTrue(cam is ParsedCommand.OpenApp)
        assertEquals(TargetApp.CAMERA, (cam as ParsedCommand.OpenApp).targetApp)

        val calc = MunnaCommandParser.parse("Calculator kholo")
        assertTrue(calc is ParsedCommand.OpenApp)
        assertEquals(TargetApp.CALCULATOR, (calc as ParsedCommand.OpenApp).targetApp)

        val custom = MunnaCommandParser.parse("Spotify kholo")
        assertTrue(custom is ParsedCommand.OpenCustomAppByName)
        assertEquals("spotify", (custom as ParsedCommand.OpenCustomAppByName).requestedAppName)
    }

    @Test
    fun parse_timeAndGreetingCommands_recognizesLocalActions() {
        val timeCmd = MunnaCommandParser.parse("Munna, abhi kitne baje hain?")
        assertTrue(timeCmd is ParsedCommand.TellTime)

        val listenCmd = MunnaCommandParser.parse("Munna, meri baat suno.")
        assertTrue(listenCmd is ParsedCommand.GreetingOrListen)
    }

    @Test
    fun parse_generalQuestion_routesToAiConversationHandler() {
        val questionCmd = MunnaCommandParser.parse("ताजमहल किस शहर में स्थित है?")
        assertTrue(questionCmd is ParsedCommand.AskAiQuestion)
    }
}
