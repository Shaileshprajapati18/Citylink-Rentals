package com.example.citylinkrentals.model;

   import com.google.gson.annotations.SerializedName;

   import java.io.Serializable;

public class User implements Serializable {
       @SerializedName("firebaseUid")
       private String firebaseUid;
       @SerializedName("username")
       private String username;
       @SerializedName("email")
       private String email;
       @SerializedName("phoneNumber")
       private String phoneNumber;

       public User() {}

       public User(String firebaseUid, String username, String email, String phoneNumber) {
           this.firebaseUid = firebaseUid;
           this.username = username;
           this.email = email;
           this.phoneNumber = phoneNumber;
       }

       public String getFirebaseUid() {
           return firebaseUid;
       }
       public void setFirebaseUid(String firebaseUid)
       { this.firebaseUid = firebaseUid;
       }
       public String getUsername()
       { return username;
       }
       public void setUsername(String username)
       { this.username = username;
       }
       public String getEmail()
       { return email;
       }
       public void setEmail(String email)
       { this.email = email;
       }
       public String getPhoneNumber()
       { return phoneNumber;
       }
       public void setPhoneNumber(String phoneNumber)
       { this.phoneNumber = phoneNumber;
        }
   }