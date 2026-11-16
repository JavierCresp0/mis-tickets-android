import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  Button,
  Image,
  Alert,
  StyleSheet,
} from 'react-native';
import { Picker } from '@react-native-picker/picker';

// import galeria y ficheros
import * as ImagePicker from 'expo-image-picker';
import { Paths, File } from 'expo-file-system';

const HomeScreen = ({ route, navigation }) => {

  const [photo, setPhoto] = useState(null);
  const [description, setDescription] = useState('');
  const [dateTime, setDateTime] = useState('');
  const [category, setCategory] = useState('Gasolina');

  useEffect(() => {
    if (route.params?.newPhoto) {
      // Guardamos la foto
      setPhoto(route.params.newPhoto);
      // Guardamos la fecha y hora del sistema
      setDateTime(new Date().toLocaleString());
    }
  }, [route.params?.newPhoto]);

  
  // Función para elegir una foto de la galería  
  const pickImage = async () => {
    const result = await ImagePicker.launchImageLibraryAsync();
    if (!result.canceled) {
      setPhoto(result.assets[0].uri);
      setDateTime(new Date().toLocaleString());
    }
  };
  
  // Función para guardar el ticket  
  const saveTicket = () => {
    if (!photo) {
      Alert.alert('Falta la foto', 'Primero haz o elige una foto');
      return;
    }

    // Crear el objeto ticket 
    const ticket = {
      photo: photo,
      description: description,
      dateTime: dateTime,
      category: category,
    };  
    const fichero = new File(Paths.document, 'tickets.json');

    // Leer los tickets 
    let tickets = [];
    if (fichero.exists) {
      tickets = JSON.parse(fichero.textSync());
    } else {
      fichero.create();
    }

    // Añadir el nuevo ticket y guardar la lista en el fichero
    tickets.push(ticket);
    fichero.write(JSON.stringify(tickets));

    Alert.alert('Guardado', 'Ticket guardado. Total: ' + tickets.length);
  };

  return (
    <View style={styles.container}>
      {/* Foto */} 
      {photo ? (
        <Image source={{ uri: photo }} style={styles.image} />
      ) : (
        <Text style={styles.noPhoto}>No hay foto aún</Text>
      )}

      {/* Descripción */}
      <Text style={styles.label}>Descripción</Text>
      <TextInput
        style={styles.input}
        placeholder="Escribe una descripción"
        value={description}
        onChangeText={setDescription}
      />

      {/* Fecha y hora */}
      <Text style={styles.label}>Fecha y hora</Text>
      <Text style={styles.dateTime}>
        {dateTime ? dateTime : '-'}
      </Text>

      {/* Categoría */}
      <Text style={styles.label}>Categoría</Text>
      <Picker
        selectedValue={category}
        onValueChange={(value) => setCategory(value)}
      >
        <Picker.Item label="Gasolina" value="Gasolina" />
        <Picker.Item label="Alimentación" value="Alimentación" />
        <Picker.Item label="Ocio" value="Ocio" />
        <Picker.Item label="Varios" value="Varios" />
      </Picker>

      {/* Botón cámara */}
      <View style={styles.buttonContainer}>
        <Button
          title="Tomar foto"
          onPress={() => navigation.navigate('Camera')}
        />
      </View>

      {/* Botón galería */}
      <View style={styles.buttonContainer}>
        <Button
          title="Elegir de galería"
          onPress={pickImage}
        />
      </View>

      {/* Botón ticket */}
      <View style={styles.buttonContainer}>
        <Button
          title="Guardar ticket"
          onPress={saveTicket}
        />
      </View>

      {/* Botón tickets */}
      <View style={styles.buttonContainer}>
        <Button
          title="Ver tickets"
          onPress={() => navigation.navigate('TicketList')}
        />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    justifyContent: 'center',
  },
  noPhoto: {
    fontSize: 22,
    textAlign: 'center',
    marginBottom: 20,
  },
  image: {
    width: '100%',
    height: 200,
    borderRadius: 10,
    marginBottom: 20,
  },
  label: {
    fontSize: 16,
    marginTop: 10,
  },
  input: {
    borderWidth: 1,
    borderColor: '#999',
    padding: 10,
    borderRadius: 5,
    marginTop: 5,
  },
  dateTime: {
    fontSize: 16,
    marginTop: 5,
    color: '#333',
  },
  buttonContainer: {
    marginTop: 20,
  },
});

export default HomeScreen;
