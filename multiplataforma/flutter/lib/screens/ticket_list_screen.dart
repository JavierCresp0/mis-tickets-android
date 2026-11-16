import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';

class TicketListScreen extends StatefulWidget {
  const TicketListScreen({super.key});

  @override
  State<TicketListScreen> createState() => _TicketListScreenState();
}

class _TicketListScreenState extends State<TicketListScreen> {
  // Lista de tickets leída del fichero
  List<dynamic> tickets = [];

  @override
  void initState() {
    super.initState();
    cargarTickets();
  }

  Future<void> cargarTickets() async {
    // Carpeta y fichero JSON
    final carpeta = await getApplicationDocumentsDirectory();
    final fichero = File('${carpeta.path}/tickets.json');

    // Leer y cargar los tickets
    if (await fichero.exists()) {
      final contenido = await fichero.readAsString();
      setState(() {
        tickets = jsonDecode(contenido);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Listado de Tickets')),

      body: Padding(
        padding: const EdgeInsets.all(15),

        child: tickets.isEmpty
            ? const Center(
                child: Text(
                  'No hay tickets todavía',
                  style: TextStyle(fontSize: 20),
                ),
              )
            : ListView.builder(
                itemCount: tickets.length,

                itemBuilder: (context, index) {
                  final ticket = tickets[index];

                  return Card(
                    margin: const EdgeInsets.only(bottom: 15),

                    child: Padding(
                      padding: const EdgeInsets.all(10),

                      child: Column(
                        children: [
                          // Foto del ticket
                          Image.file(File(ticket['photo']), height: 200),

                          const SizedBox(height: 10),

                          // Descripción
                          Text(
                            ticket['descripcion'],
                            style: const TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                            ),
                          ),

                          // Categoría y fecha
                          Text(ticket['categoria']),
                          Text(ticket['fechaHora']),
                        ],
                      ),
                    ),
                  );
                },
              ),
      ),
    );
  }
}
