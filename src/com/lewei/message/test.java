package com.lewei.message;


public class test{


	public static void main(String[] args) {
        
         ComputeSmsData sms = new ComputeSmsData();
        SerialToGsm stg = new SerialToGsm("COM4");

        String retStr = new String("");
        String sss = new String();
        String alarmNumber = new String("18896807726");

        // check for messages
        retStr = stg.checkSms();
        if (retStr.indexOf("ERROR") == -1) {
            System.out.println("Phone # of sender: " + stg.readSmsSender());
            System.out.println("Recv'd SMS message: " + stg.readSms());
        }
       
        // send a message
        sss = stg.sendSms(alarmNumber,"���Զ��ţ�");

	}

}