package jacob.daniel.jdsecuritysolutions;

//Course: CENG319
//Team: JD Security Solutions
//Author: Jacob Arsenault N01244276

public class User{
    public String name;
    public String username;
    public String password;
    public String email;

    public User(String name, String user, String pass, String email){
        this.name=name;
        this.username=user;
        this.password=pass;
        this.email=email;
    }
    public User(){
        this.name="";
        this.username="";
        this.password="";
        this.email="";
    }

    public User(String name, String pass){
        this.name="";
        this.username=name;
        this.password=pass;
        this.email="";
    }

    public User(String email){
        this.name=email;
        this.username=email;
        this.password="";
        this.email=email;
    }
}