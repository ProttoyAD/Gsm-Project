package com.example.gsm.lab.SmsController;

import com.fazecast.jSerialComm.SerialPort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;

import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Controller
public class SmsController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/send-sms")
    public String sendSms(@RequestParam String message, @RequestParam String phoneNumber) {
        String portName = "COM6";
        int baudRate = 9600;

        SerialPort serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(baudRate);
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(1);
        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);

        if (!serialPort.openPort()) {
            return "error";
        }

        InputStream inputStream = serialPort.getInputStream();
        OutputStream outputStream = serialPort.getOutputStream();

        try {
            outputStream.write(("AT+CMGF=1\r\n").getBytes());
            Thread.sleep(100);

            outputStream.write(("AT+CMGS=\"" + phoneNumber + "\"\r\n").getBytes());
            Thread.sleep(100);

            outputStream.write((message + "\r\n").getBytes());
            Thread.sleep(100);

            outputStream.write(26);
            Thread.sleep(100);

            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);

            if (len > 0) {
                String response = new String(buffer, 0, len);
                boolean smsSent = true;
                return "sms_sent";
            } else {
                return "error";
            }
        } catch (IOException | InterruptedException e) {
            return "error";
        } finally {
            // Close the serial port
            serialPort.closePort();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(SmsController.class);



    @GetMapping("/online")
    public String onlineIndex() {
        return "onlineIndex";
    }

    @PostMapping("/send")
    public String send(@RequestParam("from") String from,
                     @RequestParam("to") String to,
                     @RequestParam("body") String body) {

        VonageClient client = VonageClient.builder()
                .apiKey("bdf904da")
                .apiSecret("xR5xIhqNW5fEc2pE")
                .build();

        TextMessage message = new TextMessage(from, to, body);

        SmsSubmissionResponse response = client.getSmsClient().submitMessage(message);

        if (response.getMessages().get(0).getStatus() == MessageStatus.OK) {
            return "sms_sent";
        } else {
            return "not_sent";
        }
    }

}




