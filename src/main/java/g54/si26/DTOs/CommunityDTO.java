package g54.si26.DTOs;


/**
 * DTO para filas de la tabla Community.
 * Los alias SQL "communityId" y "name" deben coincidir con estos nombres de campo
 * para que Database#executeQueryPojo los mapee correctamente.
 */
public class CommunityDTO {

    private int    communityId;
    private String name;

    public CommunityDTO() {}

    public CommunityDTO(int communityId, String name) {
        this.communityId = communityId;
        this.name        = name;
    }

    public int    getCommunityId()               { return communityId; }
    public void   setCommunityId(int communityId){ this.communityId = communityId; }
    public String getName()                      { return name; }
    public void   setName(String name)           { this.name = name; }

    @Override
    public String toString() { return name; }
}