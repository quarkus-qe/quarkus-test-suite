package io.quarkus.qe.hibernate.items;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "account") // import script expects lower case table name, identifiers are quoted, hence case-sensitive
public class Account {

    @Id
    public Long id;

    @Column(length = 255, unique = true, nullable = false)
    @NotNull
    @Size(max = 255)
    public String email;

    public Instant createdOn;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "account_in_role", joinColumns = @JoinColumn(name = "accountid"), inverseJoinColumns = @JoinColumn(name = "roleid"))
    public Set<Role> roles = new HashSet<>();

}
