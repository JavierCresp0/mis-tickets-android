import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import HomeScreen from './src/screens/HomeScreen';
import CameraScreen from './src/screens/CameraScreen';
import TicketListScreen from './src/screens/TicketListScreen';

const Stack = createNativeStackNavigator();

const App = () => {
  return (
    <NavigationContainer>
      <Stack.Navigator initialRouteName="Home">
        <Stack.Screen
          name="Home"
          component={HomeScreen}
          options={{ title: 'Mis Tickets' }}
        />
        <Stack.Screen
          name="Camera"
          component={CameraScreen}
          options={{ title: 'Cámara' }}
        />
        <Stack.Screen
          name="TicketList"
          component={TicketListScreen}
          options={{ title: 'Listado de Tickets' }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default App;
