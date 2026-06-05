package cn.edu.sdu.java.server.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(	name = "teacher",
        uniqueConstraints = {
        })
public class Teacher {
    @Id
    private Integer personId;

    @OneToOne
    @JoinColumn(name="personId")
    private Person person;

    @Size(max = 20)
    private String title;

    @Size(max = 10)
    private String degree;

    // Getters and Setters
    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }
}
