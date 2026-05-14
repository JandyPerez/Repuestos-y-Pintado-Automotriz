package com.example.puestosypintado.Database;
import javax.swing.*;
import java.sql.*;

public class Conexion {
    public Object estabecerConexion;
    Connection connection = null;

    String usuario = "Intellij";
    String contrasena = "12345678";
    String db = "RepuestosPinturas";//"Agenda";
    // server = "localhost";
    String ip = "localhost";
    String puerto = "1433";

    String cadena = "jdbc:sqlserver://" + ip + "." + puerto + "/" + db;

    public void InsertarDatos(){
        String sql = "Insert into Persona(Nombre, Apellido, Direccion, Telefono, Email, Status) values(?,?,?,?,?,?)";

        try{
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1,"Carolina");
            pstmt.setString(2,"Herrera");
            pstmt.setString(3,"Caracas, Venezuela");
            pstmt.setString(4,"8097245700");
            pstmt.setString(5,"carolinaherrera@perfum.com");
            pstmt.setString(6, "activo");

            int filasInsertadas = pstmt.executeUpdate();
            System.out.println("Filas insertadas: " + filasInsertadas);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog
                    (null,"Error al insertar los datos:" + e.toString());
        }
    }

    public void Borrar(int id){
        String query = "Delete from Persona where id = ?;";

        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1,id);

            int filasBorradar = pstmt.executeUpdate();
            System.out.println("Registro borrado: " + filasBorradar);

        } catch (Exception e) {
            JOptionPane.showMessageDialog
                    (null,"Error al borrar datos:" + e.toString());
        }
    }

    public void ActualizarDatos(int id){
        String sql = "Update Persona set Nombre = ?, Apellido = ?, Direccion = ?, Telefono = ?, Email = ?, Status = ? where id = ?;";

        try{
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1,"Roberto");
            pstmt.setString(2,"Musso");
            pstmt.setString(3,"Montevideo, Uruguay");
            pstmt.setString(4,"4737003002");
            pstmt.setString(5,"Roberto@gmail.com");
            pstmt.setString(6, "activo");
            pstmt.setInt(7,id);

            int filasInsertadas = pstmt.executeUpdate();
            System.out.println("Filas actualizadas: " + filasInsertadas);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog
                    (null,"Error al actualizar los datos:" + e.toString());
        }
    }

    public void leerDatos(){
        String query = "Select * from Persona;";

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()){
                System.out.println("Codigo: "+rs.getInt("id"));
                System.out.println("Nombre: "+rs.getString("Nombre")+"\n");
            }

            System.out.println();

        } catch (Exception e) {
            JOptionPane.showMessageDialog
                    (null,"Error al obtener datos:" + e.toString());
        }
    }

    public Connection estabecerConexion (){
        try{
            String cadena  = "jdbc:sqlserver://" + ip + ":" + puerto + ";" + "databaseName=" + db + ";" + "encrypt=true"
                    + ";" + "trustServerCertificate=true";

            connection = DriverManager.getConnection(cadena, usuario, contrasena);
            //JOptionPane.showMessageDialog(null, "la conexion fue exitosa.");
        } catch (Exception e) {
            System.out.println(e.toString());
            JOptionPane.showMessageDialog(null,"Error en la conexion " + e.toString());
        }
        return connection;
    }
}