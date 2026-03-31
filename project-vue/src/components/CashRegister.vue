<template>
  <div class="cash-register">
    <h1>Caisse Enregistreuse Avancée</h1>
    <form @submit.prevent="handleSubmit">
      <div>
        <label>Type de transaction:</label>
        <select v-model="transactionType">
          <option value="vente">Vente</option>
          <option value="achat">Achat</option>
          <option value="les-deux">Les deux</option>
        </select>
      </div>
      <div>
        <label>Nom du client:</label>
        <input type="text" v-model="customerName">
      </div>
      <div>
        <label>Articles:</label>
        <div v-for="(item, index) in items" :key="index" class="item-row">
          <input type="text" v-model="item.name" placeholder="Nom de l'article">
          <input type="number" v-model="item.quantity" placeholder="Quantité" min="1">
          <input type="number" v-model="item.price" placeholder="Prix unitaire" step="0.01">
          <button type="button" @click="removeItem(index)">Supprimer</button>
        </div>
        <button type="button" @click="addItem">Ajouter Article</button>
      </div>
      <div class="total-display">
        <label>Montant total: {{ totalAmount }} €</label>
      </div>
      <div>
        <label>Méthode de paiement:</label>
        <select v-model="paymentMethod">
          <option value="tpe">TPE</option>
          <option value="cache">Cache</option>
          <option value="carte">Sur la carte</option>
        </select>
      </div>
      <button type="submit">Enregistrer Transaction</button>
    </form>
    <div v-if="receipt" class="receipt" ref="receiptRef">
      <h2>Reçu</h2>
      <p>Date: {{ new Date().toLocaleString() }}</p>
      <p>Type: {{ transactionType }}</p>
      <p>Client: {{ customerName || 'Anonyme' }}</p>
      <ul>
        <li v-for="item in items" :key="item.name">
          {{ item.name }} - Qté: {{ item.quantity }} - Prix: {{ item.price }}€ - Total: {{ (item.quantity * item.price).toFixed(2) }}€
        </li>
      </ul>
      <p><strong style="color: #28a745; font-size: 1.3em;">Total: {{ totalAmount }} €</strong></p>
      <p>Paiement: {{ paymentMethod }}</p>
    </div>
    <div v-if="receipt">
      <button @click="exportImage">Exporter en Image</button>
      <button @click="exportPDF">Exporter en PDF</button>
    </div>
    <div class="history">
      <h2>Historique des Transactions</h2>
      <div v-for="(trans, index) in transactionHistory" :key="index" class="transaction-item">
        <p>{{ trans.date }} - {{ trans.type }} - {{ trans.total }}€ - {{ trans.payment }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'

const transactionType = ref('vente')
const customerName = ref('')
const items = ref([{ name: '', quantity: 1, price: 0 }])
const paymentMethod = ref('cache')
const receipt = ref(false)
const receiptRef = ref(null)
const transactionHistory = ref([])

const totalAmount = computed(() => {
  return items.value.reduce((total, item) => total + (item.quantity * item.price), 0).toFixed(2)
})

const addItem = () => {
  items.value.push({ name: '', quantity: 1, price: 0 })
}

const removeItem = (index) => {
  if (items.value.length > 1) {
    items.value.splice(index, 1)
  }
}

const handleSubmit = () => {
  if (items.value.some(item => !item.name || item.quantity <= 0 || item.price <= 0)) {
    alert('Veuillez remplir tous les champs des articles correctement.')
    return
  }
  receipt.value = true
  const transaction = {
    date: new Date().toLocaleString(),
    type: transactionType.value,
    customer: customerName.value || 'Anonyme',
    items: [...items.value],
    total: totalAmount.value,
    payment: paymentMethod.value
  }
  transactionHistory.value.unshift(transaction)
  saveToLocalStorage()
  // Reset form
  customerName.value = ''
  items.value = [{ name: '', quantity: 1, price: 0 }]
  transactionType.value = 'vente'
  paymentMethod.value = 'cache'
}

const exportImage = async () => {
  if (receiptRef.value) {
    const canvas = await html2canvas(receiptRef.value)
    const link = document.createElement('a')
    link.download = 'recu.png'
    link.href = canvas.toDataURL()
    link.click()
  }
}

const exportPDF = () => {
  if (receiptRef.value) {
    const pdf = new jsPDF()
    pdf.html(receiptRef.value, {
      callback: function (pdf) {
        pdf.save('recu.pdf')
      },
      x: 10,
      y: 10
    })
  }
}

const saveToLocalStorage = () => {
  localStorage.setItem('transactionHistory', JSON.stringify(transactionHistory.value))
}

const loadFromLocalStorage = () => {
  const saved = localStorage.getItem('transactionHistory')
  if (saved) {
    transactionHistory.value = JSON.parse(saved)
  }
}

onMounted(() => {
  loadFromLocalStorage()
})
</script>

<style scoped>
.cash-register {
  max-width: 700px;
  margin: 20px auto;
  padding: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  color: white;
}

h1 {
  text-align: center;
  margin-bottom: 30px;
  font-size: 2.5em;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

form {
  background: rgba(255, 255, 255, 0.1);
  padding: 25px;
  border-radius: 10px;
  backdrop-filter: blur(10px);
}

form div {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  font-size: 1.1em;
}

input, select {
  width: 100%;
  padding: 12px;
  box-sizing: border-box;
  border: none;
  border-radius: 8px;
  font-size: 1em;
  background: rgba(255, 255, 255, 0.9);
  color: #333;
}

input:focus, select:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.5);
}

.item-row {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 15px;
  background: rgba(255, 255, 255, 0.1);
  padding: 10px;
  border-radius: 8px;
}

.item-row input {
  flex: 1;
  background: rgba(255, 255, 255, 0.9);
}

.item-row input[type="number"] {
  flex: 0 0 100px;
}

.total-display {
  background: #28a745;
  color: white;
  padding: 15px;
  border-radius: 10px;
  text-align: center;
  font-size: 1.5em;
  font-weight: bold;
  margin: 20px 0;
  box-shadow: 0 5px 15px rgba(40, 167, 69, 0.4);
}

button {
  padding: 12px 20px;
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1em;
  font-weight: bold;
  transition: all 0.3s ease;
  margin-right: 10px;
  margin-bottom: 10px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

button[type="submit"] {
  background: linear-gradient(45deg, #28a745, #20c997);
  width: 100%;
  margin-top: 20px;
}

.receipt {
  margin-top: 30px;
  padding: 25px;
  background: rgba(255, 255, 255, 0.95);
  color: #333;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.receipt h2 {
  color: #667eea;
  margin-bottom: 15px;
}

.receipt ul {
  list-style-type: none;
  padding: 0;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
}

.receipt li {
  margin-bottom: 8px;
  padding: 5px 0;
  border-bottom: 1px solid #dee2e6;
}

.receipt p strong {
  color: #28a745;
  font-size: 1.2em;
}

.history {
  margin-top: 40px;
  background: rgba(255, 255, 255, 0.1);
  padding: 25px;
  border-radius: 10px;
}

.history h2 {
  color: #fff;
  margin-bottom: 20px;
  text-align: center;
}

.transaction-item {
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  padding: 15px;
  margin-bottom: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.transaction-item:hover {
  transform: translateY(-2px);
}
</style>