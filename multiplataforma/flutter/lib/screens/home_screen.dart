import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path_provider/path_provider.dart';

import 'ticket_list_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final ImagePicker picker = ImagePicker();

  // Datos del formulario
  String? photo; 
  final TextEditingController controller = TextEditingController();
  String fechaHora = '';
  String categoria = 'Gasolina';

  // Función cámara
  Future<void> takePhoto() async {
    final XFile? foto = await picker.pickImage(source: ImageSource.camera);

    if (foto != null) {
      setState(() {
        photo = foto.path;
        fechaHora = DateTime.now().toString();
      });
    }
  }

  // Función galería
  Future<void> elegirDeGaleria() async {
    final XFile? foto = await picker.pickImage(source: ImageSource.gallery);

    if (foto != null) {
      setState(() {
        photo = foto.path;
        fechaHora = DateTime.now().toString();
      });
    }
  }

  // Función guardar ticket 
  Future<void> guardarTicket() async {
    // Comprobar que hay una foto
    if (photo == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Primero haz o elige una foto')),
      );
      return;
    }

    // Crear ticket 
    final ticket = {
      'photo': photo,
      'descripcion': controller.text,
      'fechaHora': fechaHora,
      'categoria': categoria,
    };

    // Carpeta y fichero JSON
    final carpeta = await getApplicationDocumentsDirectory();
    final fichero = File('${carpeta.path}/tickets.json');

    // Leer los tickets guardados
    List<dynamic> tickets = [];
    if (await fichero.exists()) {
      tickets = jsonDecode(await fichero.readAsString());
    }

    // Añadir nuevo ticket y guardar 
    tickets.add(ticket);
    await fichero.writeAsString(jsonEncode(tickets));

    // Aviso de que se ha guardado
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Ticket guardado. Total: ${tickets.length}')),
      );
    }
  }

  // Función para ir a la pantalla del listado de tickets
  void verTickets() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const TicketListScreen()),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Mis Tickets')),

      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(20),

          child: Column(
            children: [
              photo == null
                  ? const Text(
                      'No hay foto aún',
                      style: TextStyle(fontSize: 18),
                    )
                  : Image.file(File(photo!), height: 200),

              const SizedBox(height: 20),

              // Descripción
              TextField(
                controller: controller,
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                  labelText: 'Descripción',
                ),
              ),

              const SizedBox(height: 20),

              // Fecha y hora
              Text(
                'Fecha y hora: ${fechaHora == '' ? '-' : fechaHora}',
                style: const TextStyle(fontSize: 16),
              ),

              const SizedBox(height: 20),

              // Categoría
              DropdownButton<String>(
                value: categoria,
                items: const [
                  DropdownMenuItem(value: 'Gasolina', child: Text('Gasolina')),
                  DropdownMenuItem(
                    value: 'Alimentación',
                    child: Text('Alimentación'),
                  ),
                  DropdownMenuItem(value: 'Ocio', child: Text('Ocio')),
                  DropdownMenuItem(value: 'Varios', child: Text('Varios')),
                ],
                onChanged: (valor) {
                  setState(() {
                    categoria = valor!;
                  });
                },
              ),

              const SizedBox(height: 20),

              // Botón foto
              ElevatedButton(
                onPressed: takePhoto,
                child: const Text('Tomar foto'),
              ),

              const SizedBox(height: 10),

              // Botón galería
              ElevatedButton(
                onPressed: elegirDeGaleria,
                child: const Text('Elegir de galería'),
              ),

              const SizedBox(height: 10),

              // Botón guardar
              ElevatedButton(
                onPressed: guardarTicket,
                child: const Text('Guardar ticket'),
              ),

              const SizedBox(height: 10),

              // Botón ver tickets
              ElevatedButton(
                onPressed: verTickets,
                child: const Text('Ver tickets'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
