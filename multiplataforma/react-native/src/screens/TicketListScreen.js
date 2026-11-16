import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  FlatList,
  Image,
  StyleSheet,
} from 'react-native';

import { Paths, File } from 'expo-file-system';

const TicketListScreen = () => {

  const [tickets, setTickets] = useState([]);


  //lee los tickets guardados en el fichero
  useEffect(() => {
    const fichero = new File(Paths.document, 'tickets.json'); 
    if (fichero.exists) {
      const contenido = fichero.textSync();
      setTickets(JSON.parse(contenido));
    }
  }, []);


  //función que dibuja cada ticket
  const renderItem = ({ item }) => {
    return (
      <View style={styles.card}>
        {/* Foto  */}
        <Image source={{ uri: item.photo }} style={styles.image} />

        {/* Descripción */}
        <Text style={styles.description}>{item.description}</Text>

        {/* Categoría y fecha */}
        <Text style={styles.info}>{item.category}</Text>
        <Text style={styles.info}>{item.dateTime}</Text>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <Text style={styles.header}>Mis Tickets</Text>

      {/* Lista tickets */}
      <FlatList
        data={tickets}
        renderItem={renderItem}
        keyExtractor={(item, index) => index.toString()}
        ListEmptyComponent={
          <Text style={styles.empty}>No hay tickets todavía</Text>
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 15,
  },
  header: {
    fontSize: 28,
    marginBottom: 20,
    textAlign: 'center',
  },
  card: {
    backgroundColor: '#f2f2f2',
    padding: 10,
    marginBottom: 15,
    borderRadius: 10,
  },
  image: {
    width: '100%',
    height: 200,
    borderRadius: 10,
  },
  description: {
    marginTop: 10,
    fontSize: 18,
    fontWeight: 'bold',
  },
  info: {
    marginTop: 5,
    fontSize: 14,
    color: '#555',
  },
  empty: {
    textAlign: 'center',
    marginTop: 40,
    fontSize: 18,
    color: '#666',
  },
});

export default TicketListScreen;
