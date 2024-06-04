package Tabla;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Cubo.Dimension;
import Cubo.Hechos;

public class Operador {

    public static Tabla parsear(Map<Dimension, Integer> niveles, Hechos hechos, Integer index_medida) {
        // Crear una nueva tabla para los resultados
        Tabla tablaHechos = hechos.getTabla();
        Tabla nuevaTabla = new Tabla();
        
        // Crear columnas de los niveles en la nueva tabla
        for (Dimension d : niveles.keySet()) {
            Tabla tabla_dimension = d.getTabla();
            Columna<?> columna_nivel = tabla_dimension.getColumnas().get(niveles.get(d));
            ColumnaNumerica columna_fIds = (ColumnaNumerica) tablaHechos.getColumnas().get(d.getClaveForanea());

            if (columna_nivel instanceof ColumnaNumerica){
                ColumnaNumerica columnaCruce = new ColumnaNumerica(columna_nivel.getNombre());
                for (Double dato : columna_fIds.getDatos()){
                    columnaCruce.agregarDato((Double)columna_nivel.getContenidoFila(dato));
                }
                nuevaTabla.agregarColumna(columnaCruce); 
            } else {
                ColumnaString columnaCruce = new ColumnaString(columna_nivel.getNombre());
                for (Double dato : columna_fIds.getDatos()){
                    columnaCruce.agregarDato((String)columna_nivel.getContenidoFila(dato));
                }
                nuevaTabla.agregarColumna(columnaCruce); 
            }
        }
        // cargar la columna de la medida elegida de los hechos
        nuevaTabla.agregarColumna(tablaHechos.getColumnas().get(index_medida));
        nuevaTabla.cargarHeaders(); //headers
        return nuevaTabla;
        }
    
    
        public static Tabla agrupar(Tabla tabla, List<String> columnasAAgrupar, String operacionARealizar) {
            Map<List<Object>, List<Integer>> grupos = new HashMap<>();
    
            // Identificar las columnas a agrupar
            List<Columna<?>> columnas = tabla.getColumnas();
            List<Columna<?>> columnasAgrupacion = new ArrayList<>();
            for (String nombre : columnasAAgrupar) {
                for (Columna<?> col : columnas) {
                    if (col.getNombre().equals(nombre)) {
                        columnasAgrupacion.add(col);
                        break;
                    }
                }
            }
    
            // Crear los grupos
            for (int i = 0; i < tabla.getNumeroFilas(); i++) {
                List<Object> claveGrupo = new ArrayList<>();
                for (Columna<?> col : columnasAgrupacion) {
                    claveGrupo.add(col.getContenidoFila(i));
                }
    
                grupos.computeIfAbsent(claveGrupo, k -> new ArrayList<>()).add(i);
            }
    
            // Crear nueva tabla para los resultados agrupados
            Tabla tablaAgrupada = new Tabla();
    
            // Crear columnas agrupadas en la nueva tabla
            for (Columna<?> col : columnasAgrupacion) {
                if (col instanceof ColumnaString) {
                    tablaAgrupada.agregarColumna(new ColumnaString(col.getNombre()));
                } else if (col instanceof ColumnaNumerica) {
                    tablaAgrupada.agregarColumna(new ColumnaNumerica(col.getNombre()));
                }
            }
    
            // Crear columnas para las operaciones a realizar
            List<Columna<?>> columnasOperacion = new ArrayList<>();
            for (Columna<?> col : columnas) {
                if (!columnasAgrupacion.contains(col)) {
                    if (col instanceof ColumnaString) {
                        columnasOperacion.add(new ColumnaString(col.getNombre() + "_" + operacionARealizar));
                    } else if (col instanceof ColumnaNumerica) {
                        columnasOperacion.add(new ColumnaNumerica(col.getNombre() + "_" + operacionARealizar));
                    }
                }
            }
    
            // Agregar las columnas de operaciones a la nueva tabla
            for (Columna<?> col : columnasOperacion) {
                tablaAgrupada.agregarColumna(col);
            }
    
            // Agregar datos a la nueva tabla
            for (Map.Entry<List<Object>, List<Integer>> entry : grupos.entrySet()) {
                List<Object> claveGrupo = entry.getKey();
                List<Integer> indicesFilas = entry.getValue();
    
                // Agregar claves de grupo a la nueva tabla
                for (int i = 0; i < claveGrupo.size(); i++) {
                    Object valor = claveGrupo.get(i);
                    Columna<?> col = tablaAgrupada.getColumnas().get(i);
                    if (col instanceof ColumnaString) {
                        ((ColumnaString) col).agregarDato((String) valor);
                    } else if (col instanceof ColumnaNumerica) {
                        ((ColumnaNumerica) col).agregarDato((Double) valor);
                    }
                }
    
                // Aplicar operación a cada columna no agrupada y agregar el resultado a la nueva tabla
                for (Columna<?> col : columnasOperacion) {
                    String nombreOriginal = col.getNombre().replace("_" + operacionARealizar, "");
                    Columna<?> columnaOriginal = columnas.stream()
                            .filter(c -> c.getNombre().equals(nombreOriginal))
                            .findFirst()
                            .orElse(null);
    
                    if (columnaOriginal != null) {
                        Object resultado = aplicarOperacion(columnaOriginal, indicesFilas, operacionARealizar);
                        if (col instanceof ColumnaString) {
                            ((ColumnaString) col).agregarDato((String) resultado);
                        } else if (col instanceof ColumnaNumerica) {
                            ((ColumnaNumerica) col).agregarDato((Double) resultado);
                        }
                    }
                }
            }
    
            return tablaAgrupada;
        }
    
        // Método para aplicar la operación a una columna sobre un conjunto de filas
        private static Object aplicarOperacion(Columna<?> columna, List<Integer> indicesFilas, String operacion) {
            switch (operacion.toLowerCase()) {
                case "suma":
                    if (columna instanceof ColumnaNumerica) {
                        Double suma = 0.0;
                        for (int i : indicesFilas) {
                            suma += (Double) columna.getContenidoFila(i);
                        }
                        return suma;
                    }
                    break;
                case "promedio":
                    if (columna instanceof ColumnaNumerica) {
                        double suma = 0;
                        for (int i : indicesFilas) {
                            suma += (Double) columna.getContenidoFila(i);
                        }
                        return suma / indicesFilas.size();
                    }
                    break;
                case "contar":
                    break; //hay q implementar la logica del conteo y las de abajo
                case "min":
                    break;
                case "max":
                    break;
            }
            return null;
        }

        public enum TiposFiltros {
            IGUAL,
            MAYOR,
            MENOR,
            NO_IGUAL,
            ENTRE
        }
    
        public static List<Integer> filtrar( //devuelve lista de index
            Columna<?> columna, List<String> comparadores, TiposFiltros tipoFiltro
        ){
            List<Integer> filasOk = new ArrayList<Integer>();
            String comparador = comparadores.get(0);
            Integer n = 0;
            for(Object valRaw : columna.getDatos()){
                String val = valRaw.toString();
                switch (tipoFiltro) {
                    case IGUAL:
                        if(comparadores.contains(val)){
                            filasOk.add(n);
                        }
                        break;
                    case NO_IGUAL:
                        if(!comparadores.contains(val)){
                            filasOk.add(n);
                        }
                        break;
                    case MAYOR:
                        try{
                            Double valN = Double.parseDouble(val);
                            if(valN > Double.parseDouble(comparador)){
                                filasOk.add(n);
                            }
                        }catch(Exception e){
                            throw new RuntimeException("La columna debe ser de tipo numérico");
                        }
                        break;
                    case MENOR:
                        try{
                            Double valN = Double.parseDouble(val);
                            if(valN < Double.parseDouble(comparador)){
                                filasOk.add(n);
                            }
                        }catch(Exception e){
                            throw new RuntimeException("La columna debe ser de tipo numérico");
                        }
                        break;
                    case ENTRE:
                        if(comparadores.size() < 2){
                            throw new RuntimeException("Para filtrar ENTRE se deben tener al menos 2 valores");
                        }
                        try{
                            Double valN = Double.parseDouble(val);
                            if(
                                Double.parseDouble(comparadores.get(0)) < valN &&
                                valN < Double.parseDouble(comparadores.get(1))
                            ){
                                filasOk.add(n);
                            }
                        }catch(Exception e){
                            throw new RuntimeException("La columna debe ser de tipo numérico");
                        }
                        break;
                    default:
                        break;
                }
                n ++;
            }
            return filasOk;
        }
    

    // static Tabla removerDimension(){} ?

    // static Tabla sumarizar(){}

}