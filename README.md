# CrtSample
Crt sample app.

---

### Set device owner

```
$ adb shell dpm set-device-owner test.android.crt.debug/test.android.crt.MainDeviceAdminReceiver
```

#### List owners

```
$ adb shell dpm list-owners
```

#### Unset device owner

```
$ adb shell dpm remove-active-admin test.android.crt.debug/test.android.crt.MainDeviceAdminReceiver
```

#### Force stop

```
$ adb shell am force-stop test.android.crt.debug
```

---
