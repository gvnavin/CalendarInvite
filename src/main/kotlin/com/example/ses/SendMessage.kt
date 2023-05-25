package com.example.ses

import com.example.ses.SesAccessor.sendEmail
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties
import javax.activation.DataHandler
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import software.amazon.awssdk.services.ses.model.SesException
import javax.mail.util.ByteArrayDataSource


val formatter = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss")

/**
 *
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 */

//reference: https://shekhargulati.com/2020/09/22/sending-outlook-calendar-invite-using-java-mail-api/
object SendMessageAttachment {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val sender = ""
        val recipient = ""
        val subject = "test"

        val name = "Name"

        val bodyHTML = """
                    <html>
                        <head></head>
                        <body>
                            <h1>Hello ${name}!</h1>
                            <p>You are invited to attend the .</p>
                        </body>
                    </html>
                """.trimIndent()

        try {
            sendEmailAttachment(sender, recipient, subject, bodyHTML)
            println("Done")
        } catch (e: IOException) {
            println("main IOException e = ${e}")
            e.stackTrace
        } catch (e: MessagingException) {
            println("main MessagingException e = ${e}")
            e.stackTrace
        }
    }


    @Throws(AddressException::class, MessagingException::class, IOException::class)
    fun sendEmailAttachment(
        sender: String,
        recipient: String,
        subject: String,
        bodyHTML: String,
    ) {
        val session = Session.getDefaultInstance(Properties())

        // Create a new MimeMessage object.
        val mimeMessage = MimeMessage(session)

        // Add subject, from and to lines.
        mimeMessage.setSubject(subject, "UTF-8")
        mimeMessage.setFrom(InternetAddress(sender))
        mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
        mimeMessage.setRecipients(Message.RecipientType.CC, InternetAddress.parse(recipient))
        mimeMessage.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(recipient))

        val multipart = MimeMultipart("mixed")

        val htmlMimeBodyPart = MimeBodyPart()
        htmlMimeBodyPart.setContent(bodyHTML, "text/html; charset=UTF-8")

        val calendarRequest = CalendarRequest(
            subject = subject,
            body = "This is a test event",
            fromEmail = sender,
            toEmail = recipient,
            meetingStartTime = LocalDateTime.now(),
            meetingEndTime = LocalDateTime.now().plusHours(1),
            location = "GMeet or Platform",
            organizerName = "Organizer"
        )
        val calendarMimeBodyPart = createCalendar(calendarRequest)

        multipart.addBodyPart(htmlMimeBodyPart)
        multipart.addBodyPart(calendarMimeBodyPart)

        mimeMessage.setContent(multipart)

        try {
            sendEmail(mimeMessage)
        } catch (e: SesException) {
            println("sendEmailAttachment exception e = ${e}")
            System.err.println(e.awsErrorDetails().errorMessage())
        }
        println("Email sent using SesClient with attachment")
    }

}

fun createCalendar(calendarRequest: CalendarRequest): MimeBodyPart {

    val mimeBodyPart = MimeBodyPart()


    val calendarContent = """
                    BEGIN:VCALENDAR
                    METHOD:REQUEST
                    PRODID:Microsoft Exchange Server 2010
                    VERSION:2.0
                    BEGIN:VTIMEZONE
                    TZID:Asia/Kolkata
                    END:VTIMEZONE
                    BEGIN:VEVENT
                    ATTENDEE;ROLE=REQ-PARTICIPANT;RSVP=TRUE:MAILTO:
                    ORGANIZER;CN=${calendarRequest.organizerName}:MAILTO:${calendarRequest.fromEmail}
                    DESCRIPTION;LANGUAGE=en-US:${calendarRequest.body}
                    UID:${calendarRequest.uid}
                    SUMMARY;LANGUAGE=en-US:${calendarRequest.subject}
                    DTSTART:${formatDate(calendarRequest.meetingStartTime)}
                    DTEND:${formatDate(calendarRequest.meetingEndTime)}
                    CLASS:PUBLIC
                    PRIORITY:5
                    DTSTAMP:20200922T105302Z
                    TRANSP:OPAQUE
                    STATUS:CONFIRMED
                    SEQUENCE:0
                    LOCATION;LANGUAGE=en-US:${calendarRequest.location}
                    BEGIN:VALARM
                    DESCRIPTION:REMINDER
                    TRIGGER;RELATED=START:-PT15M
                    ACTION:DISPLAY
                    END:VALARM
                    END:VEVENT
                    END:VCALENDAR""".trimIndent()

    mimeBodyPart.setHeader("Content-Class", "urn:content-classes:calendarmessage")
    mimeBodyPart.setHeader("Content-ID", "calendar_message")

    val byteArrayDataSource = ByteArrayDataSource(calendarContent, "text/calendar;method=REQUEST;name=\"invite.ics\"")

    mimeBodyPart.dataHandler = DataHandler(byteArrayDataSource)

    return mimeBodyPart

}

private fun formatDate(localDateTime: LocalDateTime): String {
    return formatter.format(localDateTime).replace(" ", "T")
}

