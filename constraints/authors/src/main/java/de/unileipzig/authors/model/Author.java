package de.unileipzig.authors.model;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import cz.jirutka.validator.spring.SpELAssertList;
import javax.persistence.ManyToOne;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
@SpELAssertList({ @cz.jirutka.validator.spring.SpELAssert(value = "!isBookAuthor.equals(isMagazineAuthor)", message = "you have to choose one side"), @cz.jirutka.validator.spring.SpELAssert(value = "password.equals(passwordConfirm)", message = "Password confirmation failed!") })
public class Author {

    /**
     */
    @NotNull
    private String name;

    /**
     */
    private String surname;

    /**
     */
    private int age;

    /**
     */
    @NotNull
    private String password;

    /**
     */
    @NotNull
    private String passwordConfirm;

    /**
     */
    private Boolean isBookAuthor;

    /**
     */
    private Boolean isMagazineAuthor;

    /**
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "leadauthor")
    private Set<Book> books = new HashSet<Book>();
}
