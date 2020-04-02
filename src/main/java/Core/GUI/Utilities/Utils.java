package Core.GUI.Utilities;

public class Utils {
    public static class nString {
        // Java support Integer.toHexString or toBinaryString
        // but it does not support it for short

        public static String hexToString8(int value) {
            StringBuilder sb = new StringBuilder();
            sb.append(Integer.toHexString(value));

            while(sb.length() > 2) {
                sb.deleteCharAt(0);
            }

            while(sb.length() < 2) {
                sb.insert(0, "0");
            }

            sb.insert(0, "0x");
            return sb.toString();
        }

        public static String hexToString16(int value) {
            StringBuilder sb = new StringBuilder();
            sb.append(Integer.toHexString(value));

            while(sb.length() < 4) {
                sb.insert(0, "0");
            }

            sb.insert(0, "0x");

            return sb.toString();
        }

        public static String binaryToString16(int value) {
            StringBuilder sb = new StringBuilder();
            sb.append(Integer.toBinaryString(value));

            while(sb.length() < 16) {
                sb.insert(0, "0");
            }

            sb.insert(0, "0b");

            return sb.toString();
        }

        public static String binaryToString8(int value) {
            StringBuilder sb = new StringBuilder();
            sb.append(Integer.toBinaryString(value));

            while(sb.length() < 8) {
                sb.insert(0, "0");
            }

            sb.insert(0, "0b");

            return sb.toString();
        }

        public static String binaryToStringSpaced16(int value) {
            StringBuilder sb = new StringBuilder();
            sb.append(binaryToString16(value));

            //sb.delete(0, 2); // remove 0b

            for(int i = 1; i < 4; i++) {
                sb.insert(sb.length() - ((4 * i) + (i == 1 ? 0 : i - 1)), "_");
            }

            return sb.toString();
        }
    }
}
