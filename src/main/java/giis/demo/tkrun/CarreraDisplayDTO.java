package giis.demo.tkrun;

/**
 * DTO para la presentacion de carreras.
 */
public class CarreraDisplayDTO {
	private String id;
	private String descr;
	private String estado; // correspond a 'abierta' en la query

	public CarreraDisplayDTO() {}

	public CarreraDisplayDTO(String id, String descr, String estado) {
		this.id = id;
		this.descr = descr;
		this.estado = estado;
	}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getDescr() { return descr; }
	public void setDescr(String descr) { this.descr = descr; }

	public String getEstado() { return estado; }
	public void setEstado(String estado) { this.estado = estado; }
}
