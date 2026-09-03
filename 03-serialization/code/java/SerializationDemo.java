// System Design - Backend
// Chapter 03, Serialization -> Serialization in Java
// Java 21 / Jackson (bundled with Spring Boot)

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;

// ABSTRACTION: Serializer names a capability -- turn an object into
// bytes and back -- without naming a format. Callers depend on this
// contract, so JSON could be swapped underneath them for anything.
interface Serializer {
    String serialize(Object value) throws JsonProcessingException;
    
    <T> T deserialize(String data, Class<T> type)
        throws JsonProcessingException;
}

// POLYMORPHISM: one implementation among many possible. A YAML or
// Protobuf codec could be dropped in wherever a Serializer is used.
class JsonSerializer implements Serializer {
    
    // ObjectMapper is the workhorse Spring Boot already uses for
    // every @RequestBody it reads and every response it writes.
    private final ObjectMapper mapper = new ObjectMapper()
                                            .registerModule(new JavaTimeModule()); // for Instant
    
    @Override
    public String serialize(Object value) throws JsonProcessingException {
        return mapper.writeValueAsString(value); // object -> JSON
    }
    
    @Override
    public <T> T deserialize(String data, Class<T> type)
        throws JsonProcessingException {
        return mapper.readValue(data, type); // JSON -> object
    }
}

// INHERITANCE: the fields every entity shares are declared once here.
// A subclass extends it and its JSON inherits them.
abstract class BaseModel {
    @JsonProperty("id")
    protected int id;
    
    @JsonProperty("created_at") // the JSON key, not the Java name
    protected Instant createdAt;
}

// ENCAPSULATION: annotations decide the JSON shape. @JsonIgnore is
// Java's way of saying "this must never leave the object" -- the
// field exists and works, the encoder simply never sees it.
class User extends BaseModel { // IS-A BaseModel
    
    @JsonProperty("name")
    String name;
    
    @JsonProperty("active")
    boolean active;
    
    @JsonProperty("address")
    Address address; // nested object
    
    @JsonIgnore
    String password; // never serialized
}

class Address {
    @JsonProperty("country")
    String country;
    
    @JsonProperty("phone")
    int phone;
}

public class SerializationDemo {
    public static void main(String[] args) throws Exception {
        Serializer codec = new JsonSerializer(); // program to the
        // contract, not the
        // implementation
        Address home = new Address();
        home.country = "India";
        home.phone = 123456;
        
        User user = new User();
        user.id = 1;
        user.createdAt = Instant.now();
        user.name = "Ada";
        user.active = true;
        user.address = home;
        user.password = "never-serialized";
        
        // SERIALIZE -- a native Java object into the common format
        System.out.println(codec.serialize(user));
        // {"id":1,"created_at":"...","name":"Ada","active":true,
        // "address":{"country":"India","phone":123456}}
        // note what is absent: password
        
        // DESERIALIZE -- JSON arriving over HTTP, back into an object
        String incoming = "{\"name\":\"Lin\","
                              + "\"address\":{\"country\":\"IN\",\"phone\":42}}";
        User back = codec.deserialize(incoming, User.class);
        System.out.println(back.name + " " + back.address.country); // Lin IN
    }
}
