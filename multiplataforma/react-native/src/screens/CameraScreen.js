import React, { useState, useRef } from 'react';
import {
  View,
  Text,
  Button,
  StyleSheet,
} from 'react-native';

/*
  Importar Camera
*/
import { CameraView, useCameraPermissions } from 'expo-camera';

const CameraScreen = ({ navigation }) => {
  /*
    Referencia a la cámara
    useRef permite acceder al componente
  */
  const cameraRef = useRef(null);

  /*
    Hook de permisos Expo Camera
  */
  const [permission, requestPermission] = useCameraPermissions();

  /*
    Estado para saber si la cámara está lista
  */
  const [isReady, setIsReady] = useState(false);

  /*
    Si los permisos todavía están cargando
  */
  if (!permission) {
    return (
      <View style={styles.center}>
        <Text>Cargando permisos...</Text>
      </View>
    );
  }

  /*
    Si los permisos NO están concedidos
  */
  if (!permission.granted) {
    return (
      <View style={styles.center}>
        <Text style={styles.message}>
          Necesitamos permisos para usar la cámara
        </Text>
        <Button
          title="Conceder permisos"
          onPress={requestPermission}
        />
      </View>
    );
  }

  /*
    Función para tomar la foto
  */
  const takePhoto = async () => {
    // Verificar referencia
    if (!cameraRef.current) {
      return;
    }

    try {
      // Capturar imagen
      const photo = await cameraRef.current.takePictureAsync();
      console.log(photo);

      /*
        Volver a Home enviando la URI de la foto
      */
      navigation.navigate('Home', {
        newPhoto: photo.uri,
      });
    } catch (error) {
      console.log(error);
    }
  };

  return (
    <View style={styles.container}>
      {/* Cámara */}
      <CameraView
        ref={cameraRef}
        style={styles.camera}
        facing="back"
        onCameraReady={() => setIsReady(true)}
      >
        {/* Overlay con el botón */}
        <View style={styles.overlay}>
          <Button
            title="Hacer foto"
            onPress={takePhoto}
            disabled={!isReady}
          />
        </View>
      </CameraView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  camera: {
    flex: 1,
  },
  overlay: {
    flex: 1,
    justifyContent: 'flex-end',
    paddingBottom: 40,
    paddingHorizontal: 20,
  },
  center: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  message: {
    fontSize: 18,
    textAlign: 'center',
    marginBottom: 20,
  },
});

export default CameraScreen;
