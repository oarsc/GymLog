import { ajax, getElementById, createElement } from './custom-front-js-mods/dom-utils';
import { processCommand } from './src/commands';

const MAX_RESULTS = 500;

let backup;

async function init() {
	backup = await ajax('./read').then(res => res.json());

	const form = getElementById('filterForm');
	form.onsubmit = ev => {
		showData(resolveFilter(form));
		return false;
	}

	const commandForm = getElementById('commandForm');
	commandForm.onsubmit = ev => {
		if (commandForm.command.value) {
			let affected = processCommand(commandForm.command.value, backup, resolveFilter(form));
			getElementById('commandSummary').textContent = `Last command affected ${affected} rows.`;
			showData(resolveFilter(form));
		}
		return false;
	}

	getElementById('exportBtn').onclick = exportData;
	getElementById('showIdsBtn').onclick = ev => {
		showFilteredIds(resolveFilter(form));
		return false;
	}
	getElementById('showNotesBtn').onclick = ev => {
		showFilteredNotes(resolveFilter(form));
		return false;
	}
}

function showData(filter) {
	disableButtons(true);

	const parent = getElementById('code');
	parent.clear();

	const filtered = backup.bits.filter(filter);
	if (filtered.length > MAX_RESULTS) {
		getElementById('resultsSummary').textContent = `Showing ${MAX_RESULTS} out of ${filtered.length} matches`;
	} else {
		getElementById('resultsSummary').textContent = `${filtered.length} matches`;
	}
	filtered.slice(0,MAX_RESULTS)
			.forEach(bit =>
				createElement('pre', 0, parent).textContent = JSON.stringify(bit));

	disableButtons(false);
}

function resolveFilter(form) {
	const filters = [];

	let vExerciseName = form.exerciseName.value;
	let vNote = form.note.value;
	let vTrainingId = form.trainingId.value;

	if (vNote) {
		let negate;
		if (negate = vNote.startsWith('!')) vNote = vNote.substr(1);
		filters.push(negate? (bit => vNote != bit.note) : (bit => vNote == bit.note))
	}
	if (vExerciseName) {
		let negate;
		if (negate = vExerciseName.startsWith('!')) vExerciseName = vExerciseName.substr(1);
		filters.push(negate? (bit => bit.exerciseName.indexOf(vExerciseName) < 0) : (bit => bit.exerciseName.indexOf(vExerciseName) >= 0));
	}
	if (vTrainingId) {
		let negate;
		if (negate = vTrainingId.startsWith('!')) vTrainingId = vTrainingId.substr(1);
		const trainingIds = vTrainingId.split(',').map(value => parseInt(value));
		filters.push(negate? (bit => trainingIds.indexOf(bit.trainingId) < 0) : (bit => trainingIds.indexOf(bit.trainingId) >= 0));
	}

	return bit => filters.every(filter => filter(bit));
}

function exportData() {
	const parent = getElementById('code');
	parent.clear();

	createElement('div', 0, parent).textContent = JSON.stringify(backup);
}

function showFilteredIds(filter) {
	const parent = getElementById('code');
	parent.clear();

	createElement('div', 0, parent).textContent =  backup.bits
		.filter(filter)
		.map(bit => bit.trainingId)
		.unique()
		.join(',');
}

function showFilteredNotes(filter) {
	const parent = getElementById('code');
	parent.clear();

	createElement('div', 0, parent).innerHTML =  backup.bits
		.filter(filter)
		.map(bit => bit.note)
		.unique()
		.join('<br/>');
}

function disableButtons(disableValue) {
	['filterBtn','showIdsBtn','commandBtn','exportBtn', 'showNotesBtn']
		.map(getElementById)
		.forEach(button => button.disabled = disableValue);
}

init().then(() => showData(() => true))
