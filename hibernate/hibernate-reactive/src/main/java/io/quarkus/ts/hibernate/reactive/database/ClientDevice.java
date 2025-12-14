package io.quarkus.ts.hibernate.reactive.database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.validator.constraints.IpAddress;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

@Entity
@Table(name = "client_device")
public class ClientDevice extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public Integer id;

    @IpAddress(type = IpAddress.Type.IPv4)
    public String ipv4;

    @IpAddress(type = IpAddress.Type.IPv6)
    public String ipv6;
}
