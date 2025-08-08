# Steganography & TEA Encryption Tool (Java)

## Overview

This Java desktop application enables **secure message embedding and extraction** using a combination of:

- **TEA (Tiny Encryption Algorithm)** symmetric encryption for message confidentiality  
- **LSB (Least Significant Bit) Steganography** for hiding encrypted or plain messages inside images  

The project features a **modern, intuitive Swing GUI** for easy interaction, supporting message encryption, embedding into images, extraction, and decryption — all in one tool.

---

## Features

- Encrypt plaintext messages using TEA with a password-derived key  
- Embed plaintext or encrypted messages into 24-bit BMP/PNG images using LSB steganography  
- Extract and decrypt messages from stego images  
- Password-protected encryption ensures message confidentiality  
- Clean, user-friendly GUI with file selection, input fields, and status dialogs  
- Supports PNG, JPG, BMP image files for steganography (automatically handles format)  
- PKCS7 padding implemented for secure block cipher operation  
- Modular, extensible codebase designed for future audio/video steganography support  

---

## Steps To execute

1. Select an image file (PNG/JPG/BMP) for embedding or extraction.  
2. Enter the message and password in the GUI.  
3. Use the buttons to encrypt, embed, extract, or decrypt messages.  
4. Save the stego image after embedding.  
5. Extract and decrypt the message from the saved image.  

---
