@echo off
cd /d "%~dp0web"
title VIAJE CUSCO - VERSION CON TOURS
echo VERSION CON TOURS 1.1
echo Carpeta que se esta abriendo:
cd
echo.
echo Abre exactamente: http://localhost:8080/
echo Deja esta ventana abierta.
py -m http.server 8080 --bind 127.0.0.1
pause
