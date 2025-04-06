import * as http from '/http.js'
import { components } from '/components.js'
import { withCss } from '/components/Loader.js'

function formatBlockState(state) {
    let result = state.block;
    if (state.properties) {
        result += '[';
        let names = Object.getOwnPropertyNames(state.properties);
        names.sort();
        result += names.map(n => `${n}=${state.properties[n]}`).join(',');
        result += ']';
    }
    return result;
}

export function createComponent(template) {
    const args = {
        template: template,
        created() {
            http.get('/api/schematica').then(response => {
                this.config = response;
                this.onConfigLoaded();
            });
        },
        data() {
            return {
                config: null,
                schematic: null,
                slots: null,
                placing: {
                    flipX: false,
                    flipY: false,
                    flipZ: false,
                    rotateX: 0,
                    rotateY: 0,
                    rotateZ: 0
                }
            };
        },
        methods: {
            beginEdit(item) {
                item.editing = true;
                item.editText = item.formatBlockState;
            },
            clear() {
                http.delete('/api/schematica-place/_');
            },
            getFile() {
                return new Promise((resolve) => {
                    let input = this.$refs.fileInput;
                    if (input.files.length == 0) {
                        resolve(null);
                        return;
                    }
    
                    let file = input.files[0];
                    let reader = new FileReader();
                    reader.onload = event => resolve({
                        name: file.name,
                        file: event.target.result.split(',', 2)[1]
                    });
                    reader.readAsDataURL(file);
                });
            },
            onConfigLoaded() {
                this.slots = this.config.autoSelectSlots.join(',');
            },
            onFileSelected() {
                this.getFile().then(file => {
                    if (file == null) {
                        this.schematic = null;
                        return;
                    }
                    http.post('/api/schematica-upload', file).then(response => {
                        if (response.error) {
                            alert(response.error);
                            return;
                        }

                        this.schematic = response;
                        this.schematic.paletteMap = [];
                        for (let i = 0; i < this.schematic.palette.length; i++) {
                            if (this.schematic.summary[i] > 0) {
                                this.schematic.paletteMap.push({
                                    id: i,
                                    count: this.schematic.summary[i],
                                    raw: this.schematic.palette[i].raw,
                                    state: this.schematic.palette[i].state,
                                    stateFormatted: formatBlockState(this.schematic.palette[i].state)
                                });
                            }
                        }
                    }).catch(error => {
                        alert(error.message + '\n' + error.response);
                    });
                });
            },
            onSlotsUpdate() {
                let slots = this.slots.trim().split(',').filter(s => s).map(s => parseInt(s));
                if (slots.some(i => isNaN(i))) {
                    this.onConfigLoaded();
                    alert('Invalid format');
                    return;
                }
                if (slots.some(i => i <= 0 || i >= 10)) {
                    this.onConfigLoaded();
                    alert('Slot number out of range. Use 1..9');
                    return;
                }
                this.config.autoSelectSlots = slots;
                this.update();
            },
            place() {
                this.getFile().then(file => {
                    file.placing = this.placing;
                    file.palette = this.schematic.paletteMap;
                    http.post('/api/schematica-place', file);
                });
            },
            update() {
                http.post('/api/schematica', this.config).then(response => {
                    this.config = response;
                    this.onConfigLoaded();
                });
            }
        }
    };

    components.add(args, 'Radio');
    components.add(args, 'SwitchCheckbox');

    return withCss(import.meta.url, args);
}