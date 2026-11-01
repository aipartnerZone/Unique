package com.example

import com.example.domain.parser.MunnaCommandParser
import com.example.domain.parser.ParsedCommand
import com.example.domain.parser.TargetApp
import org.junit.Assert.assertEquals
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
