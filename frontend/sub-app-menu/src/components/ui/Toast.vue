<script setup>
import { computed } from 'vue';
import { CheckCircle, XCircle } from 'lucide-vue-next';

const props = defineProps({
  show: Boolean,
  message: String,
  type: {
    type: String,
    default: 'success'
  }
});

const isSuccess = computed(() => props.type === 'success');
</script>

<template>
  <Transition
    enter-active-class="transform ease-out duration-300 transition"
    enter-from-class="translate-y-2 opacity-0 sm:translate-y-0 sm:translate-x-2"
    enter-to-class="translate-y-0 opacity-100 sm:translate-x-0"
    leave-active-class="transition ease-in duration-100"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div v-if="show" class="fixed top-4 right-4 z-[100] max-w-sm w-full bg-white shadow-lg rounded-lg pointer-events-auto ring-1 ring-black ring-opacity-5 overflow-hidden">
      <div class="p-4">
        <div class="flex items-start">
          <div class="flex-shrink-0">
            <CheckCircle v-if="isSuccess" class="h-6 w-6 text-green-400" aria-hidden="true" />
            <XCircle v-else class="h-6 w-6 text-red-400" aria-hidden="true" />
          </div>
          <div class="ml-3 w-0 flex-1 pt-0.5">
            <p class="text-sm font-medium text-gray-900">
              {{ isSuccess ? 'Success' : 'Error' }}
            </p>
            <p class="mt-1 text-sm text-gray-500">
              {{ message }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>
