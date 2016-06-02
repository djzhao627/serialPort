package com.lewei.Simple;

import java.util.Enumeration;

import javax.comm.CommPortIdentifier;

public class ShowEnableSerialPort {
	public static void main(String[] args) {
		Enumeration<?> en = CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier portId;
		while (en.hasMoreElements()) {
			portId = (CommPortIdentifier) en.nextElement();
			// 如果端口类型是串口，则打印出其端口信息
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				System.out.println(portId.getName());
			}
		}
		System.out.println("finish");
	}
}
