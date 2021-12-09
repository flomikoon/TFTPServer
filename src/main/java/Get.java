public class Get {

    private byte[] readData;
    private String name;
    private int mark = 2;
    private String mode;
    private byte[] fileByte;
    private int size;
    public Get(byte[] readData){
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
}
