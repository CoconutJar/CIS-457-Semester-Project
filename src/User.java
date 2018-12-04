import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

class User {
    public String realName;
    public String userName;
    public String password;

    DataInputStream dis;
    DataOutputStream dos;

    public ArrayList<User> friends = new ArrayList<User>();
    public ArrayList<Post> posts = new ArrayList<Post>();


    // For P2P
    public int port;


    /****
     *
     * Holds all the information of the User.
     *
     ****/
    public User(String userName, String password, DataInputStream dis, DataOutputStream dos, int port) {
        this.userName = userName;
        this.password = password;
        this.dis = dis;
        this.dos = dos;
        this.port = port + 3158;
    }

    public User(String userName, String password){
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setDis(DataInputStream dis) {
        this.dis = dis;
    }

    public void setDos(DataOutputStream dos) {
        this.dos = dos;
    }

    public String toString(){
        return ("Username: " + this.getUserName() +
                "\tPassword: " + this.getPassword());
    }

    /**
     * Sends a post for the Main Feed.
     *
     * @param post
     */
//    public void updateFeed(String post) {
//
//        try {
//            dos.writeUTF("POST" + post);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Forwards a message.
     *
     * @param msg
     */
//    public void sendMsg(String msg) {
//        try {
//            dos.writeUTF("SENT" + msg);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Sends a game invite.
     *
     * @param challenger
     */
//    public void sendBattleRequest(String challenger) {
//        try {
//            dos.writeUTF("BATTLE" + challenger);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}



