class AutocompleteSelect {
  constructor(config) {
    this.input = config.input;
    this.dropdown = config.dropdown;
    this.root = config.root || this.input.closest(".combo-box");
    this.getOptions = config.getOptions;
    this.onSelect = config.onSelect;
    this.onInputChange = config.onInputChange;
    this.emptyMessage = config.emptyMessage || "Nenhum resultado encontrado";
    this.options = [];
    this.filteredOptions = [];
    this.activeIndex = -1;
    this.selectedValue = "";
    this.isOpen = false;
    this.optionsLoaded = false;

    this.bindEvents();
  }

  bindEvents() {
    this.input.addEventListener("focus", () => {
      if (!this.input.disabled) {
        this.open();
      }
    });

    this.input.addEventListener("click", () => {
      if (!this.input.disabled) {
        this.open();
      }
    });

    this.input.addEventListener("input", async () => {
      this.selectedValue = "";
      if (typeof this.onInputChange === "function") {
        this.onInputChange(this.input.value);
      }
      await this.filterAndRender(false);
    });

    this.input.addEventListener("keydown", async (event) => {
      if (!this.isOpen && (event.key === "ArrowDown" || event.key === "ArrowUp")) {
        await this.open();
        event.preventDefault();
        return;
      }

      if (!this.isOpen) {
        return;
      }

      if (event.key === "ArrowDown") {
        event.preventDefault();
        this.moveActive(1);
      } else if (event.key === "ArrowUp") {
        event.preventDefault();
        this.moveActive(-1);
      } else if (event.key === "Enter") {
        event.preventDefault();
        if (this.filteredOptions[this.activeIndex]) {
          this.selectOption(this.filteredOptions[this.activeIndex].name);
        }
      } else if (event.key === "Escape") {
        event.preventDefault();
        this.close();
      }
    });

    document.addEventListener("click", (event) => {
      if (!this.root || !this.root.contains(event.target)) {
        this.close();
      }
    });
  }

  setEnabled(enabled) {
    this.input.disabled = !enabled;
    this.input.parentElement.classList.toggle("is-disabled", !enabled);
    if (!enabled) {
      this.reset();
    }
  }

  reset() {
    this.selectedValue = "";
    this.input.value = "";
    this.options = [];
    this.optionsLoaded = false;
    this.filteredOptions = [];
    this.activeIndex = -1;
    this.close();
  }

  setValue(value) {
    this.selectedValue = value;
    this.input.value = value;
    this.close();
  }

  async open() {
    await this.filterAndRender(false);
    this.isOpen = true;
    this.dropdown.classList.remove("hidden");
    this.input.setAttribute("aria-expanded", "true");
  }

  close() {
    this.isOpen = false;
    this.dropdown.classList.add("hidden");
    this.input.setAttribute("aria-expanded", "false");
    this.activeIndex = -1;
  }

  async loadOptions(forceReload = false) {
    if (this.optionsLoaded && !forceReload) {
      return;
    }

    this.renderLoading();
    this.options = await this.getOptions();
    this.optionsLoaded = true;
  }

  async filterAndRender(forceReload = false) {
    try {
      await this.loadOptions(forceReload);
      this.filteredOptions = filterOptionsByPrefix(
        (this.options || []).map((option) => option.name),
        this.input.value
      ).map((name) => ({ name }));

      if (!this.filteredOptions.length) {
        this.dropdown.innerHTML = `<div class="combo-empty">${this.emptyMessage}</div>`;
        this.activeIndex = -1;
      } else {
        this.activeIndex = 0;
        this.renderDropdownOptions();
      }

      this.dropdown.classList.remove("hidden");
      this.isOpen = true;
      this.input.setAttribute("aria-expanded", "true");
    } catch (error) {
      this.dropdown.innerHTML = `<div class="combo-empty">Não foi possível carregar opções</div>`;
      this.dropdown.classList.remove("hidden");
      this.isOpen = true;
      this.input.setAttribute("aria-expanded", "true");
    }
  }

  renderLoading() {
    this.dropdown.innerHTML = `<div class="combo-empty">Carregando...</div>`;
    this.dropdown.classList.remove("hidden");
    this.isOpen = true;
    this.input.setAttribute("aria-expanded", "true");
  }

  renderDropdownOptions() {
    this.dropdown.innerHTML = this.filteredOptions
      .map((option, index) => {
        const name = option.name;
        const activeClass = index === this.activeIndex ? " is-active" : "";
        const selectedClass = name === this.selectedValue ? " is-selected" : "";
        return `
          <button
            type="button"
            class="combo-option${activeClass}${selectedClass}"
            data-value="${name}"
            role="option"
            aria-selected="${name === this.selectedValue}"
          >
            ${name}
          </button>
        `;
      })
      .join("");

    Array.from(this.dropdown.querySelectorAll(".combo-option")).forEach((button) => {
      button.addEventListener("mousedown", (event) => {
        event.preventDefault();
      });

      button.addEventListener("click", () => {
        this.selectOption(button.dataset.value);
      });
    });
  }

  moveActive(step) {
    if (!this.filteredOptions.length) {
      return;
    }

    this.activeIndex = (this.activeIndex + step + this.filteredOptions.length) % this.filteredOptions.length;
    this.renderDropdownOptions();
  }

  selectOption(option) {
    this.selectedValue = option;
    this.input.value = option;
    this.close();
    this.onSelect(option);
  }
}

function createAutocompleteSelect(config) {
  return new AutocompleteSelect(config);
}
