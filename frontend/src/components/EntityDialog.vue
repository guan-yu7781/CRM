<script setup>
import { computed, reactive, watch } from 'vue';

const props = defineProps({
  open: Boolean,
  title: String,
  fields: {
    type: Array,
    default: () => []
  },
  modelValue: {
    type: Object,
    default: null
  },
  submitLabel: {
    type: String,
    default: 'Save'
  },
  optionsResolver: {
    type: Function,
    required: true
  },
  error: {
    type: String,
    default: ''
  },
  // Optional: (fieldName, value, form) => ({ fieldName: newValue, ... })
  // Called on every field change; returned object is merged into the form.
  onFieldChange: {
    type: Function,
    default: null
  }
});

const emit = defineEmits(['close', 'submit']);

const form = reactive({});

watch(
  () => [props.open, props.modelValue, props.fields],
  () => {
    props.fields.forEach(field => {
      form[field.name] = props.modelValue?.[field.name] ?? '';
    });
  },
  { immediate: true }
);

async function handleFieldInput(fieldName, value) {
  if (!props.onFieldChange) return;
  const overrides = await Promise.resolve(props.onFieldChange(fieldName, value, { ...form }));
  if (overrides && typeof overrides === 'object') {
    Object.assign(form, overrides);
  }
}

const visibleFields = computed(() => props.fields.filter(field => !field.hidden));

function submit() {
  emit('submit', { ...form });
}
</script>

<template>
  <div v-if="open" class="modal" aria-hidden="false">
    <div class="modal-backdrop" @click="emit('close')" />
    <section class="modal-card" @click.stop>
      <header class="modal-header">
        <div>
          <span class="eyebrow">Record Editor</span>
          <h2>{{ title }}</h2>
          <p class="modal-subtitle">Update the commercial and operational details below.</p>
        </div>
        <button class="ghost-button icon-button" type="button" @click="emit('close')">Close</button>
      </header>

      <form class="record-form" @submit.prevent="submit">
        <label v-for="field in visibleFields" :key="field.name" class="field" :class="{ full: field.full }">
          <span>{{ field.label }}</span>

          <textarea
            v-if="field.type === 'textarea'"
            v-model="form[field.name]"
            rows="4"
            :required="field.required"
          />

          <select
            v-else-if="field.type === 'select'"
            v-model="form[field.name]"
            :required="field.required"
            @change="handleFieldInput(field.name, form[field.name])"
          >
            <option v-if="!field.required" value="">None</option>
            <option v-if="field.required && !form[field.name]" value="" disabled>— Select —</option>
            <option v-for="option in optionsResolver(field)" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>

          <input
            v-else-if="field.type === 'market-search'"
            v-model="form[field.name]"
            type="text"
            list="frontend-market-options"
            placeholder="Search Africa or Asia market"
            :required="field.required"
          />

          <input
            v-else
            v-model="form[field.name]"
            :type="field.type || 'text'"
            :required="field.required"
          />
        </label>

        <p v-if="error" class="status-text">{{ error }}</p>
        <div class="form-footer">
          <button class="ghost-button" type="button" @click="emit('close')">Cancel</button>
          <button class="primary-button" type="submit">{{ submitLabel }}</button>
        </div>
      </form>
    </section>
  </div>
</template>
