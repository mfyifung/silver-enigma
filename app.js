const STORAGE_KEY = 'silver-enigma-project-tracker';
const WORKSTREAMS = [
  { key: 'Requirement', label: 'Requirements' },
  { key: 'Design', label: 'Design' },
  { key: 'Development', label: 'Development tasks' },
  { key: 'QA', label: 'QA rounds' }
];

const form = document.getElementById('item-form');
const formTitle = document.getElementById('form-title');
const resetFormButton = document.getElementById('reset-form');
const board = document.getElementById('board');
const summaryGrid = document.getElementById('summary-grid');

let items = loadItems();

function loadItems() {
  try {
    const stored = window.localStorage.getItem(STORAGE_KEY);
    return stored ? JSON.parse(stored) : [];
  } catch {
    return [];
  }
}

function saveItems() {
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  } catch {
    return;
  }
}

function formatDate(value) {
  if (!value) {
    return 'No target date';
  }

  const [year, month, day] = value.split('-').map(Number);
  const date = new Date(year, month - 1, day);

  return date.toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
}

function createId() {
  if (window.crypto?.randomUUID) {
    return window.crypto.randomUUID();
  }

  return `item-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function resetForm() {
  form.reset();
  form.elements.id.value = '';
  form.elements.workstream.value = 'Requirement';
  form.elements.status.value = 'Planned';
  formTitle.textContent = 'Add work item';
}

function editItem(id) {
  const item = items.find((entry) => entry.id === id);
  if (!item) {
    return;
  }

  form.elements.id.value = item.id;
  form.elements.workstream.value = item.workstream;
  form.elements.title.value = item.title;
  form.elements.owner.value = item.owner;
  form.elements.status.value = item.status;
  form.elements.targetDate.value = item.targetDate;
  form.elements.notes.value = item.notes;
  const workstreamLabel = item.workstream === 'QA' ? 'QA' : item.workstream.toLowerCase();
  formTitle.textContent = `Edit ${workstreamLabel} item`;
  form.elements.title.focus();
}

function deleteItem(id) {
  items = items.filter((entry) => entry.id !== id);
  saveItems();
  render();
}

function toggleComplete(id) {
  items = items.map((entry) => {
    if (entry.id !== id) {
      return entry;
    }

    return {
      ...entry,
      status: entry.status === 'Complete' ? 'In progress' : 'Complete',
      updatedAt: new Date().toISOString()
    };
  });

  saveItems();
  render();
}

function createButton(label, onClick, className = '') {
  const button = document.createElement('button');
  button.type = 'button';
  button.textContent = label;
  button.className = className;
  button.addEventListener('click', onClick);
  return button;
}

function renderSummary() {
  summaryGrid.replaceChildren(
    ...WORKSTREAMS.map((workstream) => {
      const total = items.filter((item) => item.workstream === workstream.key).length;
      const card = document.createElement('article');
      card.className = 'summary-card';

      const count = document.createElement('strong');
      count.textContent = String(total);
      const label = document.createElement('span');
      label.textContent = workstream.label;

      card.append(count, label);
      return card;
    })
  );
}

function renderBoard() {
  board.replaceChildren(
    ...WORKSTREAMS.map((workstream) => {
      const column = document.createElement('section');
      column.className = 'workstream-column';

      const header = document.createElement('div');
      header.className = 'column-header';

      const title = document.createElement('h2');
      title.textContent = workstream.label;

      const count = document.createElement('span');
      count.className = 'pill';

      const workstreamItems = items
        .filter((item) => item.workstream === workstream.key)
        .sort((left, right) => right.updatedAt.localeCompare(left.updatedAt));

      count.textContent = `${workstreamItems.length} items`;
      header.append(title, count);
      column.append(header);

      const list = document.createElement('div');
      list.className = 'item-list';

      if (!workstreamItems.length) {
        const empty = document.createElement('p');
        empty.className = 'empty-state';
        empty.textContent = 'No items yet.';
        list.append(empty);
      }

      workstreamItems.forEach((item) => {
        const card = document.createElement('article');
        card.className = 'item-card';

        const heading = document.createElement('h3');
        heading.textContent = item.title;

        const meta = document.createElement('div');
        meta.className = 'item-meta';

        const owner = document.createElement('span');
        owner.textContent = `Owner: ${item.owner || 'Unassigned'}`;

        const due = document.createElement('span');
        due.textContent = formatDate(item.targetDate);

        const status = document.createElement('span');
        status.className = 'status-pill';
        status.dataset.status = item.status;
        status.textContent = item.status;

        meta.append(owner, due, status);

        card.append(heading, meta);

        if (item.notes) {
          const notes = document.createElement('p');
          notes.className = 'notes';
          notes.textContent = item.notes;
          card.append(notes);
        }

        const actions = document.createElement('div');
        actions.className = 'card-actions';
        actions.append(
          createButton('Edit', () => editItem(item.id)),
          createButton(item.status === 'Complete' ? 'Reopen' : 'Mark complete', () => toggleComplete(item.id)),
          createButton('Delete', () => deleteItem(item.id), 'delete-button')
        );

        card.append(actions);
        list.append(card);
      });

      column.append(list);
      return column;
    })
  );
}

function render() {
  renderSummary();
  renderBoard();
}

form.addEventListener('submit', (event) => {
  event.preventDefault();

  const formData = new FormData(form);
  const now = new Date().toISOString();
  const entry = {
    id: formData.get('id') || createId(),
    workstream: String(formData.get('workstream')),
    title: String(formData.get('title')).trim(),
    owner: String(formData.get('owner')).trim(),
    status: String(formData.get('status')),
    targetDate: String(formData.get('targetDate')),
    notes: String(formData.get('notes')).trim(),
    updatedAt: now
  };

  if (!entry.title) {
    form.elements.title.focus();
    return;
  }

  items = items.some((item) => item.id === entry.id)
    ? items.map((item) => (item.id === entry.id ? { ...item, ...entry } : item))
    : [{ ...entry, createdAt: now }, ...items];

  saveItems();
  render();
  resetForm();
});

resetFormButton.addEventListener('click', resetForm);

render();
resetForm();
