package io.quarkus.ts.spring.data.rest;

import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;

import java.util.Objects;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.PermissionsAllowed;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermissionsAllowed("read")
@Path("magazine-resource")
public class MagazineResource {

    @Inject
    MagazineListCrudRepository magazineRepository;

    @Inject
    MagazineJpaRepository magazineJpaRepository;

    public record MagazineDto(String oldName, String newName) {
    }

    @Path("{id}")
    @GET
    public Magazine getMagazine(@PathParam("id") long id) {
        return magazineJpaRepository.getReferenceById(id);
    }

    @Transactional(REQUIRES_NEW) // makes sure we activate transaction here
    @PUT
    public String updateMagazine(MagazineDto magazineDto) {
        Objects.requireNonNull(magazineDto);
        Objects.requireNonNull(magazineDto.newName());
        Objects.requireNonNull(magazineDto.oldName());

        var magazine = magazineRepository.findByName(magazineDto.oldName());
        var updatedMagazine = magazine.withName(magazineDto.newName());
        magazineRepository.save(updatedMagazine);
        return magazineRepository
                .findById(magazine.getId())
                .map(Magazine::getName)
                .orElseThrow(IllegalStateException::new); // we know it does exist by now
    }

}
