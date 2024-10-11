package com.mqtt;

public class ModbusCRC16 {

    public static String calculateCRC16(String input) {
        byte[] bytes = input.getBytes();
        int uchCRCHi = 0xFF;
        int uchCRCLo = 0xFF;
        int uIndex;

        for (byte b : bytes) {
            uIndex = uchCRCLo ^ b;
            uchCRCLo = uchCRCHi ^ (0xFF & auchCRCHi[uIndex]);
            uchCRCHi = 0xFF & auchCRCLo[uIndex];
        }

        return String.format("%02X%02X", uchCRCLo, uchCRCHi);
    }

    private static final int[] auchCRCHi = new int[256];
    private static final int[] auchCRCLo = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            int crc = i;
            for (int j = 0; j < 8; j++) {
                boolean xorFlag = (crc & 0x0001) == 1;
                crc = crc >> 1;
                if (xorFlag) {
                    crc = crc ^ 0xA001;
                }
            }
            auchCRCHi[i] = crc >> 8;
            auchCRCLo[i] = crc & 0xFF;
        }
    }

    public static void main(String[] args) {
        String input = "010504020001";
        String crc16 = calculateCRC16(input);
        System.out.println("CRC16: " + crc16);
    }
}
