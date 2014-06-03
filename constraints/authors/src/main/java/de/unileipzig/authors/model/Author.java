package de.unileipzig.authors.model;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
@NotEqualInnerClass(field1 = "name", field2 = "surname")
@NotEqual(field = "surname", scope = "test1")
//@NotEqualIdee(field1 = "name", reference = "books", field2 = "title")
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
    @NotNull
    private String password;

    /**
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "leadauthor")
    private Set<Book> books = new HashSet<Book>();
}
