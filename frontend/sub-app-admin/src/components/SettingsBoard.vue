<template>
  <div class="max-w-4xl mx-auto space-y-6">
     <Toast :show="showToast" :message="toastMessage" :type="toastType" />
     <div class="bg-card rounded-2xl shadow-sm border border-border p-8">
        <h2 class="text-2xl font-bold text-card-foreground mb-6">General Settings</h2>
        
        <div class="space-y-6">
           <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- Timezone Setting -->
              <div class="space-y-2">
                 <label class="block text-sm font-medium text-muted-foreground">Store Timezone</label>
                 <select v-model="settings.TIMEZONE" 
                         class="block w-full rounded-md border-input bg-background text-foreground shadow-sm focus:border-ring focus:ring-ring sm:text-sm p-2.5 border">
                     <option v-for="zone in timezones" :key="zone" :value="zone">{{ zone }}</option>
                 </select>
                 <p class="text-xs text-muted-foreground">This affects how "Today's Revenue" is calculated.</p>
              </div>

               <!-- Table Configuration -->
               <div class="space-y-2">
                   <label class="block text-sm font-medium text-muted-foreground">Available Tables</label>
                   <input type="text" v-model="settings.TABLE_LIST" 
                          placeholder="e.g. 1,2,3,4,VIP1"
                          class="block w-full rounded-md border-input bg-background text-foreground shadow-sm focus:border-ring focus:ring-ring sm:text-sm p-2.5 border" />
                   <p class="text-xs text-muted-foreground">Comma separated list (e.g. "1,2,3,VIP"). Used for Dine-In selection.</p>
               </div>
            </div>
           
           <div class="pt-4 flex justify-end">
              <button 
                 @click="saveSettings" 
                 :disabled="saving"
                 class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 transition-colors">
                 <svg v-if="saving" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                 {{ saving ? 'Saving...' : 'Save Changes' }}
              </button>
           </div>
        </div>
     </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { Toast } from '@mini-restaurant/ui';

const saving = ref(false);
const settings = ref({
   TIMEZONE: 'UTC',
   TABLE_LIST: '1,2,3,4,5' 
});

// Toast State
const showToast = ref(false);
const toastMessage = ref('');
const toastType = ref('success');

const showNotification = (msg, type = 'success') => {
    toastMessage.value = msg;
    toastType.value = type;
    showToast.value = true;
    setTimeout(() => {
        showToast.value = false;
    }, 3000);
};

const timezones = [
   'UTC',
   'Asia/Taipei',
   'Asia/Tokyo',
   'Asia/Shanghai',
   'Asia/Singapore',
   'America/New_York',
   'America/Los_Angeles',
   'Europe/London',
   'Europe/Paris'
];

const fetchSettings = async () => {
   try {
      const token = localStorage.getItem('token');
      const res = await axios.get('/api/orders/settings', {
         headers: { Authorization: `Bearer ${token}` }
      });
      
      const data = res.data; // List of {settingKey, settingValue}
      data.forEach(item => {
         settings.value[item.settingKey] = item.settingValue;
      });
   } catch (e) {
      console.error("Failed to load settings", e);
      showNotification("Failed to load settings", "error");
   }
};

const saveSettings = async () => {
   saving.value = true;
   try {
      const token = localStorage.getItem('token');
      
      // Save TIMEZONE
      await axios.put('/api/orders/settings', 
         { settingKey: 'TIMEZONE', settingValue: settings.value.TIMEZONE },
         { headers: { Authorization: `Bearer ${token}` } }
      );

      // Save TABLE_LIST
      await axios.put('/api/orders/settings', 
         { settingKey: 'TABLE_LIST', settingValue: settings.value.TABLE_LIST },
         { headers: { Authorization: `Bearer ${token}` } }
      );
      
      showNotification('Settings saved successfully!');
   } catch (e) {
      console.error("Failed to save settings", e);
      showNotification('Failed to save settings', 'error');
   } finally {
      saving.value = false;
   }
};

onMounted(() => {
   fetchSettings();
});
</script>
