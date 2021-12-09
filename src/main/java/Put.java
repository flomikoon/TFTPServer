import java.util.ArrayList;

public class Put {

    private byte[] readData;
    private String name;
    private int mark = 2;
    private String mode;
    private byte[] fileByte;
    private int size;
    public Put(byte[] readData){
        this.readData = readData;
    }

    public String getName(){
        int newMark = mark;
        while (readData[newMark] != 0){
            newMark++;
        }

        byte[] massNameByte = new byte[newMark - 2];
        for (int i = 0 ; i < massNameByte.length ; i ++){
            massNameByte[i] = readData[mark];
            mark++;
        }
        mark++;
        name = new String(massNameByte , 0 , massNameByte.length);
        return name;
    }

    public String getMode(){
        int newMark = mark;
        while (readData[newMark] != 0){
            newMark++;
        }

        byte[] massNameByte = new byte[newMark - mark];
        for (int i = 0 ; i < massNameByte.length ; i ++){
            massNameByte[i] = readData[mark];
            mark++;
        }
        mode = new String(massNameByte , 0 , massNameByte.length);
        return mode;
    }

    public byte[] getFileByte(int size){
        mark = 4;
        fileByte = new byte[size];
        for (int i = 0 ; i < fileByte.length ; i ++){
            fileByte[i] = readData[mark];
            mark++;
        }

        return fileByte;
    }

    public int getBlock() {
        int block = 0;

        byte[] bytes = new byte[]{readData[mark], readData[mark + 1]};
        ArrayList<Integer> bits = new ArrayList<>();

        for (byte aByte : bytes) {
            int res = aByte & 0xff;
            for (int j = 0; j < 8; j++) {
                int ch = (int) (res / Math.pow(2, 7 - j));
                if (ch == 1) {
                    res -= Math.pow(2, 7 - j);
                    bits.add(1);
                } else {
                    bits.add(0);
                }
            }
        }

        for (int i = 0 ; i < bits.size(); i++) {
            block += bits.get(i) * Math.pow(2, 15 - i);
        }

        return block;
    }

    public int getMark(){
        return size;
    }
}
