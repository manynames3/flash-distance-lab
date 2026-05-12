(function () {
  const referenceDistance = 4;
  const referencePowerStop = -4;
  const referencePower = Math.pow(2, referencePowerStop);
  const baseAperture = 5.6;
  const subjectX = 80;
  const headOffset = 5.6;
  const percentPerFoot = 4.35;

  const powerLabels = {
    "-7": "1/128",
    "-6": "1/64",
    "-5": "1/32",
    "-4": "1/16",
    "-3": "1/8",
    "-2": "1/4",
    "-1": "1/2",
    "0": "1/1"
  };

  const stage = document.getElementById("stage");
  const distanceRange = document.getElementById("distanceRange");
  const powerRange = document.getElementById("powerRange");
  const distanceValue = document.getElementById("distanceValue");
  const distanceBadge = document.getElementById("distanceBadge");
  const powerValue = document.getElementById("powerValue");
  const relativeLight = document.getElementById("relativeLight");
  const stopShift = document.getElementById("stopShift");
  const exposureTone = document.getElementById("exposureTone");
  const apertureValue = document.getElementById("apertureValue");
  const matchPower = document.getElementById("matchPower");
  const matchPowerNote = document.getElementById("matchPowerNote");
  const meterNeedle = document.getElementById("meterNeedle");
  const meterFill = document.getElementById("meterFill");
  const powerLadder = document.getElementById("powerLadder");
  const presetButtons = document.querySelectorAll("[data-distance]");
  const powerButtons = document.querySelectorAll("[data-power]");

  function clamp(value, min, max) {
    return Math.min(max, Math.max(min, value));
  }

  function trimNumber(value, digits) {
    return Number(value.toFixed(digits)).toString();
  }

  function formatMultiplier(value) {
    if (value === 1) return "1.00x";
    if (value > 1) {
      const digits = value >= 10 ? 1 : 2;
      return `${trimNumber(value, digits)}x`;
    }

    const inverse = 1 / value;
    if (inverse <= 16) {
      return `1/${trimNumber(inverse, inverse >= 10 ? 1 : 2)}`;
    }

    return `${trimNumber(value, 3)}x`;
  }

  function formatStops(stops) {
    if (Math.abs(stops) < 0.05) return "0.0 stops";
    const sign = stops > 0 ? "+" : "";
    return `${sign}${stops.toFixed(1)} stops`;
  }

  function formatAperture(value) {
    if (value < 1) return `f/${value.toFixed(1)}`;
    if (value < 10) return `f/${value.toFixed(1)}`;
    return `f/${Math.round(value)}`;
  }

  function formatNeededPower(fraction) {
    if (fraction > 1) {
      return `${trimNumber(fraction, 2)}x full`;
    }

    const exactStop = Math.log2(fraction);
    const roundedStop = Math.round(exactStop);
    const roundedFraction = Math.pow(2, roundedStop);

    if (Math.abs(fraction - roundedFraction) < 0.012) {
      return powerLabels[String(roundedStop)] || `1/${Math.round(1 / fraction)}`;
    }

    return `${Math.round(fraction * 100)}% full`;
  }

  function setActiveDistance(distance) {
    presetButtons.forEach(function (button) {
      button.classList.toggle("active", Number(button.dataset.distance) === distance);
    });
  }

  function update() {
    const distance = Number(distanceRange.value);
    const powerStop = Number(powerRange.value);
    const power = Math.pow(2, powerStop);
    const distanceRatio = distance / referenceDistance;
    const distanceOnly = Math.pow(referenceDistance / distance, 2);
    const relativeExposure = (power / referencePower) * distanceOnly;
    const stops = Math.log2(relativeExposure);
    const neededPower = referencePower * Math.pow(distanceRatio, 2);
    const neededPowerStops = Math.log2(neededPower);
    const aperture = baseAperture * Math.sqrt(relativeExposure);

    const distanceText = `${distance.toFixed(1)} ft`;
    distanceValue.value = distanceText;
    distanceBadge.textContent = distanceText;
    powerValue.value = powerLabels[String(powerStop)];

    relativeLight.textContent = formatMultiplier(relativeExposure);
    stopShift.textContent = formatStops(stops);
    apertureValue.textContent = formatAperture(aperture);
    matchPower.textContent = formatNeededPower(neededPower);

    if (Math.abs(stops) < 0.15) {
      exposureTone.textContent = "same as reference";
    } else if (stops > 0) {
      exposureTone.textContent = "brighter than reference";
    } else {
      exposureTone.textContent = "darker than reference";
    }

    if (neededPowerStops > 0.05) {
      matchPowerNote.textContent = `${formatStops(neededPowerStops - referencePowerStop)} from reference, beyond full power`;
    } else {
      matchPowerNote.textContent = `${formatStops(neededPowerStops - referencePowerStop)} from reference`;
    }

    const headGap = distance * percentPerFoot;
    const lightHeadX = subjectX - headGap;
    const flashX = lightHeadX - headOffset;
    const exposureVisual = clamp((stops + 5) / 9, 0.05, 1);
    const powerVisual = clamp((powerStop + 7) / 7, 0, 1);
    const beamAlpha = clamp(0.14 + powerVisual * 0.35 + exposureVisual * 0.24, 0.12, 0.78);
    const subjectLight = clamp(0.1 + exposureVisual * 0.9, 0.08, 1);

    stage.style.setProperty("--flash-x", `${flashX}%`);
    stage.style.setProperty("--light-head-x", `${lightHeadX}%`);
    stage.style.setProperty("--beam-length", `${headGap}%`);
    stage.style.setProperty("--distance-left", `${lightHeadX}%`);
    stage.style.setProperty("--distance-width", `${headGap}%`);
    stage.style.setProperty("--beam-alpha", beamAlpha.toFixed(3));
    stage.style.setProperty("--subject-light", subjectLight.toFixed(3));

    const meterPosition = clamp(50 + stops * 12.5, 0, 100);
    meterNeedle.style.left = `${meterPosition}%`;
    meterFill.style.width = `${meterPosition}%`;

    powerButtons.forEach(function (step) {
      step.classList.toggle("active", Number(step.dataset.power) === powerStop);
    });

    setActiveDistance(distance);
  }

  distanceRange.addEventListener("input", update);
  powerRange.addEventListener("input", update);

  presetButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      distanceRange.value = button.dataset.distance;
      update();
    });
  });

  powerButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      powerRange.value = button.dataset.power;
      update();
    });
  });

  update();
}());
