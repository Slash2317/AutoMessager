@echo off

cd /D %~dp0service

@SERVICE_NAME@.exe stop
@SERVICE_NAME@.exe uninstall