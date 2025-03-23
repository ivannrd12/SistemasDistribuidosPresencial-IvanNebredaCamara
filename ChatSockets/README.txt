Guía de Uso - Chat por Sockets TCP
==================================
Estructura del Proyecto:
------------------------
- Servidor: ChatServerImpl
- Cliente: ChatClientImpl
- Comunicación mediante objetos serializados ChatMessage

Pasos para ejecutar el sistema:
-------------------------------
1. Compilar el proyecto:
   mvn clean compile
   
2. Ejecutar el servidor:

   mvn exec:java@run-server
   - Inicia el servidor en el puerto 1500
   - Permite escribir 'shutdown' para apagarlo
   
3. Ejecutar un cliente:

   mvn exec:java@run-client -Dexec.args="servidor nickname"
   - Servidor localhost
   - Nickname: Nombre de usuario
     

4. Enviar mensajes:
   Escribir directamente en consola y pulsar Enter


Comandos disponibles desde el cliente:
--------------------------------------
- ban nombre_usuario       → Bloquea los mensajes de ese usuario
- unban nombre_usuario     → Desbloquea los mensajes de ese usuario
- logout                   → Sale del chat y cierra la conexión

Notas importantes:
------------------
- Cada mensaje se muestra como nombre: mensaje
- El apagado del servidor desconecta automáticamente a los clientes

     
     
     
     
     
     