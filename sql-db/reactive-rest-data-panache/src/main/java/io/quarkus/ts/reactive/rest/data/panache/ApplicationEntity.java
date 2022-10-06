package io.quarkus.ts.reactive.rest.data.panache;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SqlFragmentAlias;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity(name = "application")
@FilterDef(name = "useLikeByName", parameters = { @ParamDef(name = "name", type = "string") })
@Filter(name = "useLikeByName", condition = "name like :name")
@FilterDef(name = "useServiceByName", parameters = { @ParamDef(name = "name", type = "string") })
public class ApplicationEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotEmpty(message = "name can't be null")
    @Column(unique = true, nullable = false)
    public String name;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "version_id", nullable = false)
    public VersionEntity version;

    @JsonManagedReference
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Filter(name = "useServiceByName", condition = "{s}.name = :name", aliases = {
            @SqlFragmentAlias(alias = "s", table = "service")
    })
    public List<ServiceEntity> services = new ArrayList<>();

    @Transient
    public Optional<ServiceEntity> getServiceByName(String serviceName) {
        return services.stream().filter(s -> serviceName.equals(s.name)).findFirst();
    }
}
