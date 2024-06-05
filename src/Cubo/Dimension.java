package Cubo;

import Lectores.LectorArchivos;
import Tabla.Tabla;

public class Dimension {
    private String nombre;
    private Tabla tabla;
    private int clave_foranea;
    private int numeroNiveles;

    public Dimension(String nombre, String path, int clave_foranea){
        this.nombre = nombre;
        String[][] csv = LectorArchivos.leerCSV(path);

        Tabla t = new Tabla();
        t.cargarTabla(csv);

        this.tabla = t;
        this.clave_foranea = clave_foranea;
        this.numeroNiveles = tabla.getHeaders().length-1;
    }


    public String getNombre() {
        return nombre;
    }

    public int getClaveForanea(){
        return clave_foranea;
    }

    public Tabla getTabla(){
        return tabla;
    }

    public int getNumeroNiveles(){
        return numeroNiveles;
    }
}